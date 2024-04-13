package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class PurpleHazeCloudParticle extends AbstractSlowingParticle {
    private final SpriteProvider spriteProvider;
    private final float decrement;

    protected PurpleHazeCloudParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, SpriteProvider spriteProvider) {
        super(clientWorld, d, e, f, g, h, i);
        this.spriteProvider = spriteProvider;
        this.maxAge = 16;
        this.decrement = 0.5f / maxAge;
        this.scale = 1.0f;
        this.alpha = 1.0f;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(spriteProvider);
        this.scale -= decrement;
        this.alpha -= decrement;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new PurpleHazeCloudParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
