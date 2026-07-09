package net.arna.jcraft.client.sound;

import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BoundSoundInstance extends AbstractTickableSoundInstance {
    @Getter
    private final long id;
    @Getter @Setter
    private @Nullable LivingEntity boundEntity;
    private final float originalVolume;

    public BoundSoundInstance(long id, LivingEntity boundEntity, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        super(soundEvent, soundSource, RandomSource.create());
        this.id = id;
        this.boundEntity = boundEntity;
        this.volume = volume;
        originalVolume = volume;
        this.pitch = pitch;

        Vec3 pos = boundEntity.position();
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    public BoundSoundInstance(long id, Vec3 boundPosition, SoundEvent soundEvent, SoundSource soundSource,  float volume, float pitch) {
        super(soundEvent, soundSource, RandomSource.create());
        this.id = id;
        this.boundEntity = null;
        this.volume = volume;
        originalVolume = volume;
        this.pitch = pitch;

        x = boundPosition.x;
        y = boundPosition.y;
        z = boundPosition.z;
    }

    @Override
    public void tick() {
        if (boundEntity == null) return;

        if (!boundEntity.isAlive() || boundEntity.isRemoved() || boundEntity instanceof StandEntity<?,?> s &&
                (s.getUser() == null || !s.getUser().isAlive() || s.getUser().isRemoved())) {
            if (BoundSoundClient.isPendingRemoval(boundEntity) || boundEntity instanceof StandEntity<?,?> s &&
                    s.getUser() != null && BoundSoundClient.isPendingRemoval(s.getUser())) {
                // Bound entity was removed, but we're waiting for a potential re-add.
                // In the meantime, keep the sound going, but silence it.
                volume = 0f;
                return;
            }

            stop();
            return;
        }

        volume = originalVolume;

        // Update position
        Vec3 pos = boundEntity.position();
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    @Override
    public boolean canPlaySound() {
        return !isStopped();
    }

    public void cancel() {
        stop();
    }
}
