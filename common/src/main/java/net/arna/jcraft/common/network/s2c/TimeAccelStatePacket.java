package net.arna.jcraft.common.network.s2c;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.NonNull;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;

// TODO: fix time sync between server and client so it doesn't jump at the end

/**
 * Packet sent to clients when MIH's time acceleration ult is used.
 * Sent either when the ult starts or when it (for some reason) gets cancelled.
 * Also sent to clients who joined during the use of the ult. (Or at least, it should be)
 */
public class TimeAccelStatePacket {
    private static final Int2ObjectMap<TimeAcceleration> accelerations = new Int2ObjectOpenHashMap<>();
    private static final Object lock = new Object();
    private static final WeakHashMap<Level, Long> startDayTime = new WeakHashMap<>();

    public static void init() {
        // Decrease all durations.
        TickEvent.SERVER_POST.register(TimeAccelStatePacket::tick);
        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientTickEvent.CLIENT_POST.register(c -> {
                if (!c.hasSingleplayerServer()) tick(c);
            });
        }

        // Handle acceleration on server.
        TickEvent.SERVER_LEVEL_POST.register(world -> {
            if (!world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                return;
            }

            applyAcceleration(world, world::setDayTime);
        });
    }

    public static void addAcceleration(final int mihEntityId, final int duration, final long startTime) {
        synchronized (lock) {
            accelerations.put(mihEntityId, new TimeAcceleration(duration, mihEntityId, startTime));
        }
    }

    public static void removeAcceleration(final int mihEntityId) {
        synchronized (lock) {
            accelerations.remove(mihEntityId);
        }
    }

    public static void sendStart(@NonNull final MadeInHeavenEntity mih, int duration) {
        final long time = System.currentTimeMillis();

        // On singleplayer, the accelerations map is used for client stuff too.
        if (Objects.requireNonNull(mih.getEntityWorld().getServer()).isDedicatedServer()) {
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeEnum(State.START);
            buf.writeVarInt(mih.getId());
            buf.writeVarInt(duration);
            buf.writeLong(time); // for time syncing

            NetworkManager.sendToPlayers(((ServerLevel) mih.level()).players(), JPacketRegistry.S2C_TIME_ACCELERATION_STATE, buf);
        }

        synchronized (lock) {
            accelerations.put(mih.getId(), new TimeAcceleration(duration, mih.getId(), time));
        }
    }

    public static void sendStop(@NonNull final MadeInHeavenEntity mih) {
        if (Objects.requireNonNull(mih.getServer()).isDedicatedServer()) {
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeEnum(State.STOP);
            buf.writeVarInt(mih.getId());

            NetworkManager.sendToPlayers(((ServerLevel) mih.level()).players(), JPacketRegistry.S2C_TIME_ACCELERATION_STATE, buf);
        }

        synchronized (lock) {
            accelerations.remove(mih.getId());
        }
    }

    @SuppressWarnings("unused")
    private static double someBsArnaPutTogetherInDesmos(double x) {
        return Math.sqrt(1 - Math.pow(2 * x * x - 1, 2));
    }

    private static double integralOfSomeBsArnaPutTogetherInDesmos(double x) {
        double x2 = x * x;
        return (x2 - 1) * Math.sqrt(1 - x2) + 1;
    }

    public static void applyAcceleration(Level world, Consumer<Long> dayTimeSetter) {
        if (accelerations.isEmpty()) {
            startDayTime.put(world, -1L);
            return;
        }

        long start = startDayTime.getOrDefault(world, -1L);
        if (start < 0) {
            startDayTime.put(world, start = world.getDayTime());
        }

        long total = accelerations.values().stream()
                .filter(ta -> ta.isValid(world))
                .mapToLong(ta -> (long) Mth.lerp(integralOfSomeBsArnaPutTogetherInDesmos(Math.min(1,
                        (System.currentTimeMillis() - ta.getStartTime()) / (ta.getInitialDuration() * 50d))), 0, 240000))
                .sum();

        dayTimeSetter.accept(start + total);
    }

    private static void tick(Object o) {
        // Avoid thread-safety issues by locking on our lock.
        synchronized (lock) {
            if (accelerations.isEmpty()) return;

            boolean done = accelerations.values().stream()
                    .allMatch(ta -> {
                        if (ta.getDuration() > 0) {
                            ta.decrementDuration();
                        }

                        return ta.getDuration() <= 0;
                    });

            if (done) {
                accelerations.clear();
            }
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
        private final long startTime;
        private final int entityId;

        public TimeAcceleration(int duration, int entityId, long startTime) {
            this.duration = this.initialDuration = duration;
            this.entityId = entityId;
            this.startTime = startTime;
        }

        public boolean isValid(Level world) {
            return world.isClientSide() || world.getEntity(entityId) instanceof MadeInHeavenEntity;
        }

        public void decrementDuration() {
            duration--;
        }
    }
}
