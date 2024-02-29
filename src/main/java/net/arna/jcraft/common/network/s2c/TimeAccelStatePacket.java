package net.arna.jcraft.common.network.s2c;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Data;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Map;

// TODO: fix time sync between server and client so it doesn't jump at the end

/**
 * Packet sent to clients when MIH's time acceleration ult is used.
 * Sent either when the ult starts or when it (for some reason) gets cancelled.
 * Also sent to clients who joined during the use of the ult. (Or at least, it should be)
 */
public class TimeAccelStatePacket {
    public static final Int2ObjectMap<TimeAcceleration> accelerations = new Int2ObjectOpenHashMap<>();
    public static long lastUpdate = 0;
    private static final Object lock = new Object();

    static {
        // Decrease all durations.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Avoid thread-safety issues by locking on our lock.
            synchronized (lock) {
                new IntOpenHashSet(accelerations.keySet()).forEach(id -> {
                    if (accelerations.get(id).getDuration() <= 0) accelerations.remove(id);
                    else accelerations.get(id).decrementDuration();
                });
            }
        });

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

        playerManager.getPlayerList().forEach(player -> ServerPlayNetworking.send(player, JPacketRegistry.S2C_TIME_ACCELERATION_STATE, buf));

        synchronized (lock) {
            accelerations.put(mih.getId(), new TimeAcceleration(duration, mih.getId()));
        }
    }

    public static void sendStop(PlayerManager playerManager, MadeInHeavenEntity mih) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(State.STOP.ordinal());
        buf.writeVarInt(mih.getId());

        playerManager.getPlayerList().forEach(player -> ServerPlayNetworking.send(player, JPacketRegistry.S2C_TIME_ACCELERATION_STATE, buf));

        synchronized (lock) {
            accelerations.remove(mih.getId());
        }
    }

    private static double someBsArnaPutTogetherInDesmos(double x) {
        return Math.sqrt(1 - Math.pow(2 * x * x - 1, 2));
    }

    public static double getAcceleration(World world) {
        synchronized (lock) {
            return accelerations.int2ObjectEntrySet().stream()
                    // Ensure entity exists in this world
                    .filter(e -> e.getValue().isValid(world))
                    .map(Map.Entry::getValue)
                    .mapToDouble(a -> someBsArnaPutTogetherInDesmos((Util.getMeasuringTimeMs() - a.getStartTime()) /
                            (a.getInitialDuration() * 50d)))
                    .sum() * 24000;
        }
    }

    public enum State {
        START, STOP
    }

    @Data
    public static class TimeAcceleration {
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
