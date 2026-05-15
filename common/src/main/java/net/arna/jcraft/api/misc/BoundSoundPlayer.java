package net.arna.jcraft.api.misc;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Synchronized;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.client.sound.BoundSoundClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class BoundSoundPlayer {

    public static EntitySoundHandle playSoundFrom(LivingEntity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        Collection<? extends Player> listeners = getListeners(entity.level(), entity.position());
        EntitySoundHandle handle = new EntitySoundHandle(entity, sound, category, volume, pitch, listeners);
        startSoundHandle(entity.level(), handle);
        return handle;
    }

    public static PosSoundHandle playSoundAt(Level level, Vec3 pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        Collection<? extends Player> listeners = getListeners(level, pos);
        PosSoundHandle handle = new PosSoundHandle(level, pos, sound, category, volume, pitch, listeners);
        startSoundHandle(level, handle);
        return handle;
    }

    public static void stopAll(Collection<? extends SoundHandle> sounds) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(SoundHandle.TYPE_STOP);

        // Write IDs
        sounds.stream()
                .mapToLong(SoundHandle::getId)
                .distinct()
                .forEach(buf::writeVarLong);

        // Get players to send this packet to
        Set<ServerPlayer> listeners = sounds.stream()
                .flatMap(s -> s.getListeners().stream())
                .filter(p -> p instanceof ServerPlayer)
                .map(p -> (ServerPlayer) p)
                .collect(Collectors.toSet());

        NetworkManager.sendToPlayers(listeners, JPacketRegistry.S2C_BOUND_SOUND, buf);
    }

    private static Collection<? extends Player> getListeners(Level level, Vec3 position) {
        AABB box = AABB.ofSize(position, 50, 50, 50);
        return level.players().stream()
                .filter(p -> box.contains(p.position()))
                .toList();
    }

    public static void startSoundHandle(Level level, SoundHandle handle) {
        if (level.isClientSide()) {
            startSoundHandleClient(handle);
            return;
        }

        handle.play();
    }

    private static void startSoundHandleClient(SoundHandle handle) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        handle.write(buf);
        BoundSoundClient.onBoundSoundPacket(Minecraft.getInstance(), buf);
    }

    /**
     * A handle to a bound sound played on clients.
     */
    @Getter
    public static sealed class SoundHandle permits EntitySoundHandle, PosSoundHandle {
        // Slightly more efficient than an enum and only used internally by the system.
        public static final byte TYPE_STOP = 0;
        public static final byte TYPE_ENTITY = 1;
        public static final byte TYPE_POSITION = 2;

        private static long nextId = 1;

        private final long id = getNextId();
        private final SoundEvent sound;
        private final SoundSource category;
        private final float volume;
        private final float pitch;
        private final Set<Player> listeners;

        protected SoundHandle(SoundEvent sound, SoundSource category, float volume, float pitch, Collection<? extends Player> listeners) {
            this.sound = sound;
            this.category = category;
            this.volume = volume;
            this.pitch = pitch;

            Set<Player> set = Collections.newSetFromMap(new WeakHashMap<>());
            set.addAll(listeners);
            this.listeners = Collections.unmodifiableSet(set);
        }

        @Synchronized
        private static long getNextId() {
            return nextId++;
        }

        protected void write(FriendlyByteBuf buf) {
            buf.writeVarLong(id);

            // Write sound
            ResourceKey<SoundEvent> soundKey = ResourceKey.create(Registries.SOUND_EVENT, sound.getLocation());
            buf.writeResourceKey(soundKey);

            buf.writeEnum(category);
            buf.writeFloat(volume);
            buf.writeFloat(pitch);
        }

        private void play() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            write(buf);
            sendToListeners(buf);
        }

        public void stop() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeByte(TYPE_STOP);
            buf.writeVarLong(id);
            sendToListeners(buf);
        }

        private void sendToListeners(FriendlyByteBuf packet) {
            List<ServerPlayer> serverListeners = listeners.stream()
                    .filter(p -> p instanceof ServerPlayer)
                    .map(p -> (ServerPlayer) p)
                    .toList();
            NetworkManager.sendToPlayers(serverListeners, JPacketRegistry.S2C_BOUND_SOUND, packet);
        }
    }

    public static final class EntitySoundHandle extends SoundHandle {
        @Getter
        private final LivingEntity boundEntity;

        EntitySoundHandle(LivingEntity boundEntity, SoundEvent sound, SoundSource category, float volume, float pitch,
                          Collection<? extends Player> listeners) {
            super(sound, category, volume, pitch, listeners);
            this.boundEntity = boundEntity;
        }

        @Override
        protected void write(FriendlyByteBuf buf) {
            buf.writeByte(SoundHandle.TYPE_ENTITY);
            super.write(buf);
            buf.writeVarInt(boundEntity.getId());
        }
    }

    public static final class PosSoundHandle extends SoundHandle {
        @Getter
        private final Level level;
        @Getter
        private final Vec3 position;

        PosSoundHandle(Level level, Vec3 position, SoundEvent sound, SoundSource category, float volume, float pitch,
                       Collection<? extends Player> listeners) {
            super(sound, category, volume, pitch, listeners);
            this.level = level;
            this.position = position;
        }

        @Override
        protected void write(FriendlyByteBuf buf) {
            buf.writeByte(SoundHandle.TYPE_POSITION);
            super.write(buf);
            buf.writeDouble(position.x);
            buf.writeDouble(position.y);
            buf.writeDouble(position.z);
        }
    }
}
