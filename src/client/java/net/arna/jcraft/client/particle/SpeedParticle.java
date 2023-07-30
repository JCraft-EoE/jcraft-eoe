package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class SpeedParticle extends AbstractSlowingParticle {
    private final SpriteProvider spriteProvider;

    SpeedParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.scale = 0.2f + random.nextFloat() * 0.1f;
        this.maxAge = 3;
        this.setSpriteForAge(spriteProvider);
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        float c = 1f - (float) age / (float) maxAge;
        this.setColor(c * 0.2f, c, c);

        if (!this.dead)
            this.setSprite(spriteProvider.getSprite(random));
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
            return new SpeedParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
