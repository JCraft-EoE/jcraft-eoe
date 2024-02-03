package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.jetbrains.annotations.Nullable;

// POC particle. Feel free to remove.
// Any particle that renders with the JParticleTextureSheet.INVERSION_SHEET
// will be rendered with an inverted effect.
// For the time being, colors are ignored
public class InversionParticle extends AbstractSlowingParticle {

    protected InversionParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i,
                                SpriteProvider spriteProvider) {
        super(clientWorld, d, e, f, g, h, i);
        scale *= 1f;
        setSprite(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return JParticleTextureSheet.INVERSION_SHEET;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new InversionParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
        }
    }
}
