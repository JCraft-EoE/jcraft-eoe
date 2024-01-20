package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class PixelParticle extends AbstractSlowingParticle {
    PixelParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.setSpriteForAge(spriteProvider);
        this.setColor(1.0f, 0.7f, 0.4f);
        this.scale = 0.03f;
        this.maxAge = 7 + this.random.nextBetween(-3, 3);
        this.gravityStrength = 4.9f; // g / 20
        this.collidesWithWorld = true;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_LIT;
    }

    public void tick() {
        super.tick();
        float c = (float) (maxAge - age) / maxAge;
        this.setColor(1.0f, 0.5f + c * 0.5f, 0.2f + c * 0.8f);
    }

    @Override
    protected int getBrightness(float tint) {
        return 255;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new PixelParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
