package net.arna.jcraft.common.util;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.explosion.Explosion;

import java.util.function.Function;

@Data
@Builder(builderClassName = "Builder")
public class JExplosionModifier {
    @With
    private final Boolean createFire; // Has to be nullable to indicate no change.
    @With
    private final Explosion.DestructionType destructionType;
    @With
    private final ParticleEffect particle;
    @With
    private final SoundEvent sound;
    @With
    private final SoundCategory soundCategory;
    @With
    private final Function<Random, Float> volumeGetter, pitchGetter;

    public static class Builder {

        public Builder volume(float volume) {
            volumeGetter(random -> volume);
            return this;
        }

        public Builder pitch(float pitch) {
            pitchGetter(random -> pitch);
            return this;
        }
    }
}
