package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveQueue;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;


public class StandControlPacket {

    public static PacketByteBuf write(MoveQueue type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeEnumConstant(type);
        return buf;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        ServerWorld world = player.getWorld();
        MoveQueue type = buf.readEnumConstant(MoveQueue.class);

        server.execute(() -> {
            switch (type) {
                case STAND_SUMMON -> {
                    PacketByteBuf buf2 = PacketByteBufs.create();
                    buf2.writeShort(6);
                    buf2.writeInt(0);
                    ServerChannelFeedbackPacket.send(player, buf2);

                    StandComponent standData = JComponents.getStandData(player);
                    StandEntity<?, ?> stand = standData.getStand();
                    if (stand != null) {
                        int moveStun = stand.getMoveStun();
                        if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT)
                            stand.queuedAttack = MoveQueue.STAND_SUMMON;
                        else stand.desummon();
                    } else if (world != null) JCraft.summon(world, player);
                }
                case LIGHT -> {
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand == null) return;

                    initStandMove(stand, MoveQueue.LIGHT);
                }
                case UTILITY -> {
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null) initStandMove(stand, MoveQueue.UTILITY);
                    else {
                        StandEntity<?, ?> stand2 = JCraft.summon(world, player);
                        if (stand2 != null) stand2.initMove(MoveType.UTILITY);
                    }
                }
                default -> initStandOrSpecMove(player, type);
            }
        });
    }

    private static void initStandOrSpecMove(ServerPlayerEntity player, MoveQueue type) {
        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null) initStandMove(stand, type);
        else {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec == null) return;

            spec.initMove(type.getMoveType());
            if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                spec.queuedMove = type;
        }
    }

    private static void initStandMove(StandEntity<?, ?> stand, MoveQueue type) {
        int moveStun = stand.getMoveStun();

        stand.initMove(type.getMoveType());
        if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
            stand.queuedAttack = type;
    }
}
