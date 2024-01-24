package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class GoParticle extends AbstractSlowingParticle {
    protected final SpriteProvider spriteProvider;

    GoParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.maxAge = 20 + random.nextInt(10);
        setSpriteForAge(spriteProvider);
    }

    public void tick() {
        super.tick();
        this.scale = (age - 0.03f * age * age) * 0.1f;
        if (age % 5 == 0)
            setSprite(spriteProvider.getSprite(random));
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new GoParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}