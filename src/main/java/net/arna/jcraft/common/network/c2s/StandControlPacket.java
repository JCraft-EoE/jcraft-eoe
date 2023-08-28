package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.MoveQueue;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;

import static net.arna.jcraft.JCraft.QUEUE_MOVESTUN_LIMIT;
import static net.arna.jcraft.JCraft.SPEC_QUEUE_MOVESTUN_LIMIT;


public class StandControlPacket {
    public static final Identifier ID = JCraft.id("scchannel");

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        short control = buf.readShort();
        ServerWorld world = player.getWorld();

        //System.out.println("Control recieved: " + control);
        //...You will get errors related to the ref count if you try to read data on either server or client thread

        // TODO lot of boilerplate here.
        switch (control) {
            // 1 - STAND SUMMON & DESUMMON
            case 1 -> server.execute(() -> {
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
                    else
                        stand.desummon();
                } else if (world != null) JCraft.summon(world, player);
            });
            // 2 - LIGHT ATTACK
            case 2 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand == null) return;

                int moveStun = stand.getMoveStun();

                if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                    stand.queuedAttack = MoveQueue.LIGHT;
                else stand.handleMove(MoveType.LIGHT);
            });
            // 3 - BLOCK
            case 3 -> {
                boolean rmb = buf.readBoolean();
                server.execute(() -> {
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (!JCraft.isDashing(player) && stand != null) {
                        boolean blocking = stand.blocking;
                        if (!blocking && stand.canAttack() && rmb) {
                            if (player.getMainHandStack().getUseAction() == UseAction.NONE && player.getOffHandStack().getUseAction() == UseAction.NONE)
                                stand.blocking = true;
                        } else if (blocking && !rmb) stand.blocking = false;
                    }
                });
            }
            // 4 - HEAVY
            case 4 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.HEAVY;
                    else stand.initMove(MoveType.HEAVY);
                } else {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedMove = MoveQueue.HEAVY;
                        else spec.initMove(MoveType.HEAVY);
                    }
                }
            });
            // 5 - BARRAGE
            case 5 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.BARRAGE;
                    else stand.initMove(MoveType.BARRAGE);
                } else {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedMove = MoveQueue.BARRAGE;
                        else spec.initMove(MoveType.BARRAGE);
                    }
                }
            });
            // 6 - SPECIAL 1
            case 6 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.SPECIAL1;
                    else stand.initMove(MoveType.SPECIAL1);
                } else {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedMove = MoveQueue.SPECIAL1;
                        else spec.initMove(MoveType.SPECIAL1);
                    }
                }
            });
            // 7 - Ultimate
            case 7 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.ULTIMATE;
                    else stand.initMove(MoveType.ULTIMATE);
                } else {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedMove = MoveQueue.ULTIMATE;
                        else spec.initMove(MoveType.ULTIMATE);
                    }
                }
            });
            // 8 - SPECIAL 2
            case 8 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.SPECIAL2;
                    else stand.initMove(MoveType.SPECIAL2);
                } else {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedMove = MoveQueue.SPECIAL2;
                        else spec.initMove(MoveType.SPECIAL2);
                    }
                }
            });
            // 9 - SPECIAL 3
            case 9 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.SPECIAL3;
                    else stand.initMove(MoveType.SPECIAL3);
                } else {
                    JSpec<?, ?> spec = JUtils.getSpec(player);
                    if (spec != null) {
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedMove = MoveQueue.SPECIAL3;
                        else spec.initMove(MoveType.SPECIAL3);
                    }
                }
            });
            // 10 - Utility (TSTP, Explosive dash, Gun, etc.)
            case 10 -> server.execute(() -> {
                StandComponent standData = JComponents.getStandData(player);
                StandEntity<?, ?> stand = standData.getStand();
                if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.MIDDLE_MOUSE;
                    else stand.initMove(MoveType.UTILITY);
                } else {
                    StandEntity<?, ?> stand2 = JCraft.summon(world, player);
                    if (stand2 != null) stand2.initMove(MoveType.UTILITY);
                }
            });
            // 13 - Cooldown Cancel
            case 13 -> server.execute(() -> {
                if (player.isCreative() || !player.hasStatusEffect(JStatusRegistry.DAZED))
                    JComponents.getCooldowns(player).cooldownCancel();
            });
        }
    }
}
