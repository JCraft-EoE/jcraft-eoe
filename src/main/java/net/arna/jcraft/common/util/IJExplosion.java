package net.arna.jcraft.common.util;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.explosion.Explosion;

import java.util.function.Function;

public interface IJExplosion {

    void jcraft$setCreateFire(boolean createFire);

    boolean jcraft$isCreateFire();

    void jcraft$setDestructionType(Explosion.DestructionType destructionType);

    Explosion.DestructionType jcraft$getDestructionType();

    void jcraft$setParticle(ParticleEffect particle);

    ParticleEffect jcraft$getParticle();

    void jcraft$setSound(SoundEvent sound);

    SoundEvent jcraft$getSound();

    void jcraft$setSoundCategory(SoundCategory category);

    SoundCategory jcraft$getSoundCategory();

    default void jcraft$setVolume(float volume) {
        jcraft$setVolume(random -> volume);
    }

    void jcraft$setVolume(Function<Random, Float> volumeGetter);

    default void jcraft$setPitch(float pitch) {
        jcraft$setPitch(random -> pitch);
    }

    void jcraft$setPitch(Function<Random, Float> pitchGetter);
}
