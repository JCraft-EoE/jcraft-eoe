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
        this.maxAge = 7;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        private final float scale;
        public Factory(SpriteProvider spriteProvider, float scale) {
            this.spriteProvider = spriteProvider;
            this.scale = scale;
        }
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            HitsparkParticle hitsparkParticle = new HitsparkParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            hitsparkParticle.scale = this.scale;
            return hitsparkParticle;
        }
    }
}
