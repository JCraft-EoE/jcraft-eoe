package net.arna.jcraft.common.network.c2s;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

import static net.arna.jcraft.JCraft.*;

public class PlayerInputPacket {
    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                InputStateManager sm = getInputStateManager(player);

                // Handle held inputs
                sm.heldInputs.forEach(type -> handleMoveInput(server, player, type));

                int forward = sm.calcForward();
                int side = sm.calcSide();
                JComponents.getMiscData(player).updateRemoteInputs(forward, side, sm.jumping);

                StandEntity<?, ?> stand = JUtils.getStand(player);
                if (stand != null) stand.updateRemoteInputs(forward, side, sm.jumping);

                if (sm.dashing) JCraft.tryDash(forward, side, player);

                if (!sm.jumping) continue;

                if (JCraft.isDashing(player))
                    // 5s cooldown for superjumping
                    JComponents.getCooldowns(player).setCooldown(CooldownType.DASH, 100);

                checkComboBreak(player);
            }
        });
    }

    public static PacketByteBuf write(Object2BooleanMap<MovementInputType> movementInput, Object2BooleanMap<MoveInputType> moveInput) {
        PacketByteBuf buf = PacketByteBufs.create();
        writeInput(buf, movementInput);
        writeInput(buf, moveInput);
        return buf;
    }

    private static void writeInput(PacketByteBuf buf, Object2BooleanMap<? extends Enum<?>> input) {
        buf.writeVarInt(input.size());
        for (Object2BooleanMap.Entry<? extends Enum<?>> entry : input.object2BooleanEntrySet()) {
            buf.writeEnumConstant(entry.getKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        InputStateManager sm = getInputStateManager(player);
        handleMovementInput(server, player, buf, sm);
        handleMoveInput(player, buf, sm);
    }

    private static void handleMovementInput(MinecraftServer server, ServerPlayerEntity player, PacketByteBuf buf, InputStateManager sm) {
        int count = buf.readVarInt();

        for (int i = 0; i < count; i++) {
            MovementInputType type = buf.readEnumConstant(MovementInputType.class);
            boolean pressed = buf.readBoolean();

            switch (type) {
                case FORWARD -> sm.forward = pressed;
                case BACKWARD -> sm.backward = pressed;
                case LEFT -> sm.left = pressed;
                case RIGHT -> sm.right = pressed;
            }

            if (type == MovementInputType.JUMP) sm.jumping = pressed;
            if (type == MovementInputType.DASH) {
                sm.dashing = pressed;
                if (pressed) server.execute(() -> JCraft.tryDash(sm.calcForward(), sm.calcSide(), player));
            }
        }

        if (sm.jumping) server.execute(() -> checkComboBreak(player));
    }


    private static void handleMoveInput(ServerPlayerEntity player, PacketByteBuf buf, InputStateManager sm) {
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            MoveInputType type = buf.readEnumConstant(MoveInputType.class);
            boolean pressed = buf.readBoolean();

            if (type.isHoldable()) {
                if (pressed) sm.heldInputs.add(type);
                else sm.heldInputs.remove(type);
            }

            if (pressed) handleMoveInput(Objects.requireNonNull(player.getServer()), player, type);
        }
    }

    private static void handleMoveInput(MinecraftServer server, ServerPlayerEntity player, MoveInputType type) {
        ServerWorld world = player.getWorld();
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
                            stand.queueMove(MoveInputType.STAND_SUMMON);
                        else stand.desummon();
                    } else if (world != null) JCraft.summon(world, player);
                }
                case LIGHT -> {
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand == null) return;

                    initStandMove(stand, MoveInputType.LIGHT);
                }
                case UTILITY -> {
                    StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand != null) initStandMove(stand, MoveInputType.UTILITY);
                    else {
                        StandEntity<?, ?> stand2 = JCraft.summon(world, player);
                        if (stand2 != null) stand2.initMove(MoveType.UTILITY);
                    }
                }
                default -> initStandOrSpecMove(player, type);
            }
        });
    }

    private static void initStandOrSpecMove(ServerPlayerEntity player, MoveInputType type) {
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

    private static void initStandMove(StandEntity<?, ?> stand, MoveInputType type) {
        int moveStun = stand.getMoveStun();

        stand.initMove(type.getMoveType());
        if (moveStun > 0 && moveStun < QUEUE_MOVESTUN_LIMIT && !stand.isBlocking())
            stand.queueMove(type);
    }

    private static void checkComboBreak(ServerPlayerEntity player) {
        // Combo break if stunned, jumping and crouching
        InputStateManager sm = getInputStateManager(player);
        if (sm == null || !sm.jumping || !player.isSneaking() || JUtils.isBlocking(player)) return;

        StatusEffectInstance stun = player.getStatusEffect(JStatusRegistry.DAZED);
        if (stun != null) JCraft.comboBreak(player.getWorld(), player, stun);
    }

    private static InputStateManager getInputStateManager(ServerPlayerEntity player) {
        return ((IJInputStateManagerHolder) player).jcraft$getJInputStateManager();
    }
}
