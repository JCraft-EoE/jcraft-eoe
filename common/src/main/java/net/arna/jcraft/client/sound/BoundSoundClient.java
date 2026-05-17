package net.arna.jcraft.client.sound;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.EntityEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.*;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.misc.BoundSoundPlayer;
import net.arna.jcraft.common.events.JEntityEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The client part of the bound sound system.
 * This class is responsible for handling the sound packets and actually playing the sounds.
 * Binding sounds to entities is done by the sound instance itself.
 *
 * @see BoundSoundInstance
 */
public class BoundSoundClient {
    private static final Long2ObjectMap<BoundSoundInstance> playingSounds = new Long2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<List<BoundSoundInstance>> byEntity = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<EntityRemovalCountdown> removedEntities = new Int2ObjectOpenHashMap<>();

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(BoundSoundClient::tick);
        JEntityEvents.REMOVE.register(BoundSoundClient::onEntityRemoved);
        EntityEvent.ADD.register(BoundSoundClient::onEntityAdded);
    }

    private static void tick(Minecraft client) {
        SoundManager soundManager = client.getSoundManager();

        // Remove all stopped sounds.
        LongList toRemove = playingSounds.long2ObjectEntrySet().stream()
                .filter(e -> !soundManager.isActive(e.getValue()))
                .mapToLong(Long2ObjectMap.Entry::getLongKey)
                .collect(LongArrayList::new, LongArrayList::add, LongArrayList::addAll);

        toRemove.forEach(playingSounds::remove);

        // Tick down removed entities and stop their sounds if the countdown has elapsed.
        IntSet entitiesToRemove = new IntOpenHashSet();
        removedEntities.values().forEach(e -> {
            if (e.tick()) {
                List<BoundSoundInstance> sounds = byEntity.remove(e.getEntity().getId());
                if (sounds != null) sounds.forEach(soundManager::stop);
                entitiesToRemove.add(e.getEntity().getId());
            }
        });
        entitiesToRemove.forEach(removedEntities::remove);
    }

    private static EventResult onEntityAdded(Entity entity, Level level) {
        // When the user teleports, their stand is removed and added back a few ticks later.
        // We don't want to cancel the sound when this happens, so we delay the
        // cancelling by a few ticks to ensure the entity really is gone.
        // Between the remove event and this add event, though, the volume is set to 0.
        removedEntities.remove(entity.getId());

        if (entity instanceof LivingEntity le)
            byEntity.getOrDefault(entity.getId(), Collections.emptyList())
                .forEach(i -> i.setBoundEntity(le));

        return EventResult.pass();
    }

    private static void onEntityRemoved(Entity entity, Entity.RemovalReason reason) {
        // Stop bound sounds when entity is removed.
        if (byEntity.containsKey(entity.getId()))
            removedEntities.put(entity.getId(), new EntityRemovalCountdown(entity, 4));
    }

    public static boolean isPendingRemoval(Entity entity) {
        return removedEntities.containsKey(entity.getId());
    }

    /**
     * Stops a bound sound by its id if it's still active and cleans up after.
     * @param id The id of the sound to stop.
     */
    private static void stopBoundSound(long id) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        BoundSoundInstance inst = playingSounds.remove(id);
        if (inst == null || !soundManager.isActive(inst)) return;

        soundManager.stop(inst);
        LivingEntity entity = inst.getBoundEntity();
        if (entity == null) return;

        List<BoundSoundInstance> entitySounds = byEntity.getOrDefault(entity.getId(), Collections.emptyList());
        entitySounds.remove(inst);

        if (entitySounds.isEmpty()) byEntity.remove(entity.getId());
    }

    /**
     * Processes a bound sound packet.
     * Either stops the listed sounds if the type is
     * {@link net.arna.jcraft.api.misc.BoundSoundPlayer.SoundHandle#TYPE_STOP SoundHandle.TYPE_STOP},
     * or plays a new sound bound to either an entity or a position.
     * @param client The Minecraft instance that holds the sound manager to use.
     * @param buf The byte buf that contains the packet data
     */
    public static void onBoundSoundPacket(Minecraft client, FriendlyByteBuf buf) {
        Level level = client.level;

        if (level == null) return;

        byte type = buf.readByte();

        // Stop packet, stop the sound.
        if (type == BoundSoundPlayer.SoundHandle.TYPE_STOP) {
            LongSet toCancel = new LongOpenHashSet();
            while (buf.readableBytes() > 0)
                toCancel.add(buf.readVarLong());

            client.execute(() -> toCancel.forEach(BoundSoundClient::stopBoundSound));
            return;
        }

        long id = buf.readVarLong();

        ResourceKey<SoundEvent> soundKey = buf.readResourceKey(BuiltInRegistries.SOUND_EVENT.key());
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(soundKey);

        if (sound == null) {
            JCraft.LOGGER.warn("Got bound sound with invalid sound: {}.", soundKey.location());
            return;
        }

        SoundSource category = buf.readEnum(SoundSource.class);

        float volume = buf.readFloat();
        float pitch = buf.readFloat();

        BoundSoundInstance inst = switch (type) {
            case BoundSoundPlayer.SoundHandle.TYPE_ENTITY -> {
                int entityId = buf.readVarInt();
                Entity entity = level.getEntity(entityId);
                if (!(entity instanceof LivingEntity le)) {
                    JCraft.LOGGER.warn("Got bound sound with invalid entity: {}.", entityId);
                    yield null;
                }

                yield new BoundSoundInstance(id, le, sound, category, volume, pitch);
            }
            case BoundSoundPlayer.SoundHandle.TYPE_POSITION -> {
                Vec3 position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
                yield new BoundSoundInstance(id, position, sound, category, volume, pitch);
            }
            default -> {
                JCraft.LOGGER.warn("Got bound sound with invalid type: {}.", type);
                yield null;
            }
        };

        if (inst == null)
            // Warning was already logged, exit.
            return;

        playingSounds.put(id, inst);
        if (inst.getBoundEntity() != null)
            byEntity.computeIfAbsent(inst.getBoundEntity().getId(), e -> new ArrayList<>()).add(inst);
        client.execute(() -> client.getSoundManager().queueTickingSound(inst));
    }

    private static class EntityRemovalCountdown {
        @Getter
        private final Entity entity;
        private int ticks;

        public EntityRemovalCountdown(Entity entity, int ticks) {
            this.entity = entity;
            this.ticks = ticks;
        }

        public boolean tick() {
            return ticks-- == 0;
        }
    }
}
