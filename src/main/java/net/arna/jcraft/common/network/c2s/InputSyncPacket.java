package net.arna.jcraft.common.network.c2s;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MovementInputType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static net.arna.jcraft.JCraft.comboBreak;

public class InputSyncPacket {
    private static final Map<ServerPlayerEntity, StateManager> stateManagers = Collections.synchronizedMap(new WeakHashMap<>());

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (Map.Entry<ServerPlayerEntity, StateManager> entry : stateManagers.entrySet()) {
                ServerPlayerEntity player = entry.getKey();
                StateManager stateManager = entry.getValue();

                int forward = stateManager.calcForward();
                int side = stateManager.calcSide();
                JComponents.getMiscData(player).updateRemoteInputs(
                        forward, stateManager.calcSide(), stateManager.jumping);

                StandEntity<?, ?> stand = JUtils.getStand(player);
                if (stand != null) stand.updateRemoteInputs(forward, side, stateManager.jumping);

                if (!stateManager.jumping) continue;

                if (JCraft.isDashing(player))
                    // 5s cooldown for superjumping
                    JComponents.getCooldowns(player).setCooldown(CooldownType.DASH, 100);

                // Combo break if stunned, jumping and crouching
                if (player.isSneaking()) {
                    StatusEffectInstance stun = player.getStatusEffect(JStatusRegistry.DAZED);
                    if (JUtils.isBlocking(player)) return;
                    if (stun != null)
                        comboBreak(player.getWorld(), player, stun);
                }
            }
        });
    }

    public static PacketByteBuf write(Object2BooleanMap<MovementInputType> input) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(input.size());
        for (Object2BooleanMap.Entry<MovementInputType> entry : input.object2BooleanEntrySet()) {
            buf.writeEnumConstant(entry.getKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
        return buf;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        StateManager sm = stateManagers.computeIfAbsent(player, p -> new StateManager());
        int count = buf.readVarInt();
        boolean dash = false;

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
            if (type == MovementInputType.DASH) dash = true;
        }

        if (dash) JCraft.tryDash(sm.calcForward(), sm.calcSide(), player);
    }

    private static class StateManager {
        public boolean forward, backward, left, right;
        public boolean jumping;

        public int calcForward() {
            int forward = 0;
            if (this.forward) forward++;
            if (backward) forward--;
            return forward;
        }

        public int calcSide() {
            int side = 0;
            if (left) side++;
            if (right) side--;
            return side;
        }
    }
}
