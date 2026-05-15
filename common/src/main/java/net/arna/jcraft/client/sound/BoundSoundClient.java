package net.arna.jcraft.client.sound;

import dev.architectury.event.events.client.ClientTickEvent;
import it.unimi.dsi.fastutil.longs.*;
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

import java.util.*;

public class BoundSoundClient {
    private static final Long2ObjectMap<BoundSoundInstance> playingSounds = new Long2ObjectOpenHashMap<>();
    private static final Map<Entity, List<BoundSoundInstance>> byEntity = new WeakHashMap<>();

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(BoundSoundClient::tick);
        JEntityEvents.REMOVE.register(BoundSoundClient::onEntityRemoved);
    }

    private static void tick(Minecraft client) {
        SoundManager soundManager = client.getSoundManager();

        // Remove all stopped sounds.
        LongList toRemove = playingSounds.long2ObjectEntrySet().stream()
                .filter(e -> !soundManager.isActive(e.getValue()))
                .mapToLong(Long2ObjectMap.Entry::getLongKey)
                .collect(LongArrayList::new, LongArrayList::add, LongArrayList::addAll);

        toRemove.forEach(playingSounds::remove);
    }

    private static void onEntityRemoved(Entity entity, Entity.RemovalReason reason) {
        // Stop bound sounds when entity is removed.
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        List<BoundSoundInstance> sounds = byEntity.remove(entity);
        if (sounds != null) sounds.forEach(soundManager::stop);
    }

    private static void stopBoundSound(long id) {
        BoundSoundInstance inst = playingSounds.remove(id);
        if (inst == null) return;

        Minecraft.getInstance().getSoundManager().stop(inst);
        LivingEntity entity = inst.getBoundEntity();
        if (entity == null) return;

        List<BoundSoundInstance> entitySounds = byEntity.getOrDefault(entity, Collections.emptyList());
        entitySounds.remove(inst);

        if (entitySounds.isEmpty()) byEntity.remove(entity);
    }

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
            byEntity.computeIfAbsent(inst.getBoundEntity(), e -> new ArrayList<>()).add(inst);
        client.execute(() -> client.getSoundManager().queueTickingSound(inst));
    }
}
