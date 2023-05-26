package net.arna.jcraft.client.network.s2c;

import it.unimi.dsi.fastutil.ints.*;
import lombok.Data;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.MadeInHeavenEntity;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Map;

// TODO: send packet when new user joins
// TODO: fix time sync between server and client so it doesn't jump at the end

/**
 * Packet sent to clients when MIH's time acceleration ult is used.
 * Sent either when the ult starts or when it (for some reason) gets cancelled.
 * Also sent to clients who joined during the use of the ult. (Or at least, it should be)
 */
public class TimeAccelStatePacket {
    public static final Identifier ID = JCraft.id("time_accel_state");
    private static final Int2ObjectMap<TimeAcceleration> accelerations = new Int2ObjectOpenHashMap<>();
    private static long lastUpdate = 0;

    static {
        // Handle time acceleration on the client.
        // This should be done smoothly.
        WorldRenderEvents.START.register(ctx -> {
            if (!ctx.world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) return;

            double acceleration = getAcceleration(ctx.world());

            long currentTime = Util.getMeasuringTimeMs();
            if (acceleration == 0) {
                lastUpdate = currentTime;
                return;
            }

            double multiplier = (currentTime - lastUpdate) / 1000d;
            ctx.world().setTimeOfDay((long) (ctx.world().getTimeOfDay() + acceleration * multiplier));

            lastUpdate = currentTime;
        });

        // Decrease all durations.
        ServerTickEvents.END_SERVER_TICK.register(server -> new IntOpenHashSet(accelerations.keySet()).forEach(id -> {
            if (accelerations.get(id).getDuration() <= 0) accelerations.remove(id);
            else accelerations.get(id).decrementDuration();
        }));

        // Handle acceleration on server.
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) return;

            double acceleration = getAcceleration(world);
            world.setTimeOfDay((long) (world.getTimeOfDay() + acceleration * 0.05));
        });
    }

    public static void sendStart(PlayerManager playerManager, MadeInHeavenEntity mih, int duration) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(State.START.ordinal());
        buf.writeVarInt(mih.getId());
        buf.writeVarInt(duration);

        playerManager.getPlayerList().forEach(player -> ServerPlayNetworking.send(player, ID, buf));
        accelerations.put(mih.getId(), new TimeAcceleration(duration, mih.getId()));
    }

    // TODO: some kind of stop condition? Player/stand dies or something?
    public static void sendStop(PlayerManager playerManager, MadeInHeavenEntity mih) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(State.STOP.ordinal());
        buf.writeVarInt(mih.getId());

        playerManager.getPlayerList().forEach(player -> ServerPlayNetworking.send(player, ID, buf));
        accelerations.remove(mih.getId());
    }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        State state = State.values()[buf.readVarInt()];
        Entity e = client.world == null ? null : client.world.getEntityById(buf.readVarInt());

        if (!(e instanceof MadeInHeavenEntity mih) || !mih.isAlive()) return;

        switch (state) {
            case START -> accelerations.put(mih.getId(), new TimeAcceleration(buf.readVarInt(), mih.getId()));
            case STOP -> accelerations.remove(mih.getId());
        }
    }

    private static double someBsArnaPutTogetherInDesmos(double x) {
        return Math.sqrt(1 - Math.pow(2 * x * x - 1, 2));
    }

    private static double getAcceleration(World world) {
        return accelerations.int2ObjectEntrySet().stream()
                // Ensure entity exists in this world
                .filter(e -> e.getValue().isValid(world))
                .map(Map.Entry::getValue)
                .mapToDouble(a -> someBsArnaPutTogetherInDesmos((Util.getMeasuringTimeMs() - a.getStartTime()) /
                        (a.getInitialDuration() * 50d)))
                .sum() * 24000;
    }

    public enum State {
        START, STOP
    }

    @Data
    private static class TimeAcceleration {
        private int duration;
        private double lastAcceleration;
        private final int initialDuration;
        private final long startTime = Util.getMeasuringTimeMs();
        private final int entityId;

        public TimeAcceleration(int duration, int entityId) {
            this.duration = this.initialDuration = duration;
            this.entityId = entityId;
        }

        public boolean isValid(World world) {
            return duration > 0 && world.getEntityById(entityId) instanceof MadeInHeavenEntity;
        }

        public void decrementDuration() {
            duration--;
        }
    }
}
