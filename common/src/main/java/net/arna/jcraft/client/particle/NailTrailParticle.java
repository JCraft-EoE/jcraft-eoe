package net.arna.jcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class NailTrailParticle extends TextureSheetParticle {
    private final SpriteSet spriteProvider;

    protected NailTrailParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;

        // Set lifespan to 30 ticks
        this.lifetime = 30;

        // Small size that doesn't change
        this.quadSize = 0.15f;

        // Blue-ish color (cyan/light blue)
        this.rCol = 0.4f;
        this.gCol = 0.7f;
        this.bCol = 1.0f;

        // Start fully opaque
        this.alpha = 1.0f;

        // No gravity
        this.gravity = 0.0f;

        // Don't move
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();

        // Calculate age ratio (0.0 at start, 1.0 at end)
        float ageRatio = (float) this.age / (float) this.lifetime;

        // Fade out over lifetime
        this.alpha = 1.0f - ageRatio;

        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float tint) {
        // Full brightness like glowing particles
        return 240 | 240 << 16;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new NailTrailParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
        }
    }
}