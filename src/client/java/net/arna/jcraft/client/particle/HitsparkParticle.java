package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class HitsparkParticle extends JGlowingParticle {
    HitsparkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
    }

    @Override
    protected void initialize() {
        this.alpha = 1f;
        this.scale = 0.5f;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        private final float scale;
        private final int maxAge;
        public Factory(SpriteProvider spriteProvider, float scale, int maxAge) {
            this.spriteProvider = spriteProvider;
            this.scale = scale;
            this.maxAge = maxAge;
        }
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            HitsparkParticle hitsparkParticle = new HitsparkParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            hitsparkParticle.scale = this.scale;
            hitsparkParticle.maxAge = this.maxAge;
            return hitsparkParticle;
        }
    }
}
