package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.old.MoveQueue;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
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
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();
                    stand.initLightAttack();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.LIGHT;
                }
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

                    stand.initHeavyAttack();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.HEAVY;
                } else {
                    JCraftSpec spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.initHeavyAttack(world);
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedAttack = MoveQueue.HEAVY;
                    }
                }
            });
            // 5 - BARRAGE
            case 5 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    stand.initBarrage();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.BARRAGE;
                } else {
                    JCraftSpec spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.initBarrage(world);
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedAttack = MoveQueue.BARRAGE;
                    }
                }
            });
            // 6 - SPECIAL 1
            case 6 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    stand.initSpecial1();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.SPECIAL1;
                } else {
                    JCraftSpec spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.initSpecial1(world);
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedAttack = MoveQueue.SPECIAL1;
                    }
                }
            });
            // 7 - Ultimate
            case 7 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    stand.initUlt();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.ULTIMATE;
                } else {
                    JCraftSpec spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.initUlt(world);
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedAttack = MoveQueue.ULTIMATE;
                    }
                }
            });
            // 8 - SPECIAL 2
            case 8 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    stand.initSpecial2();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.SPECIAL2;
                } else {
                    JCraftSpec spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.initSpecial2(world);
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedAttack = MoveQueue.SPECIAL2;
                    }
                }
            });
            // 9 - SPECIAL 3
            case 9 -> server.execute(() -> {
                StandEntity<?, ?> stand = JUtils.getStand(player);
	            if (stand != null) {
                    int moveStun = stand.getMoveStun();

                    stand.initSpecial3();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.SPECIAL3;
                } else {
                    JCraftSpec spec = JUtils.getSpec(player);
                    if (spec != null) {
                        spec.initSpecial3(world);
                        if (spec.moveStun > 0 && spec.moveStun < SPEC_QUEUE_MOVESTUN_LIMIT)
                            spec.queuedAttack = MoveQueue.SPECIAL3;
                    }
                }
            });
            // 10 - Utility (TSTP, Explosive dash, Gun, etc.)
            case 10 -> server.execute(() -> {
                StandComponent standData = JComponents.getStandData(player);
                StandEntity<?, ?> stand = standData.getStand();
                if (stand != null) {
                    int moveStun = stand.getMoveStun();
                    stand.initUtil();
                    if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
                        stand.queuedAttack = MoveQueue.MIDDLE_MOUSE;
                } else {
                    StandEntity<?, ?> stand2 = JCraft.summon(world, player);
                    if (stand2 != null) stand2.initUtil();
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
