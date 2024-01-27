package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class BlocksparkParticle extends AbstractSlowingParticle {
    protected final SpriteProvider spriteProvider;

    BlocksparkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setColor(0.3f, 0.9f, 1.0f);
        this.maxAge = 6;
        setSpriteForAge(spriteProvider);
    }

    public void tick() {
        super.tick();
        if (age % 2 == 0)
            setSprite(spriteProvider.getSprite(random));
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getBrightness(float tint) {
        return 255;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        private final float scale;

        public Factory(SpriteProvider spriteProvider, float scale) {
            this.spriteProvider = spriteProvider;
            this.scale = scale;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            BlocksparkParticle hitsparkParticle = new BlocksparkParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            hitsparkParticle.scale = this.scale;
            return hitsparkParticle;
        }
    }
}