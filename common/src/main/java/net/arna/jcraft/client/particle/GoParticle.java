package net.arna.jcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class GoParticle extends RisingParticle {
    protected final SpriteSet spriteProvider;
    private final float rotationPerTick;

    private static final float MAX_ROTATION = (float) (Math.PI / 12); // 15 degrees total

    GoParticle(final ClientLevel world, final double x, final double y, final double z, final double velocityX, final double velocityY, final double velocityZ, final SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.lifetime = 20 + random.nextInt(10);
        this.rotationPerTick = MAX_ROTATION / this.lifetime * (random.nextBoolean() ? 1 : -1);
        this.roll = 0;
        this.oRoll = 0;
        setSpriteFromAge(spriteProvider);
    }

    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        this.roll = this.rotationPerTick * this.age;
        this.alpha = Math.max(0f, 1f - (float) this.age / (float) this.lifetime);
        this.quadSize = (age - 0.03f * age * age) * 0.033f;
        if (age % 5 == 0) {
            setSprite(spriteProvider.get(random));
        }
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(final SimpleParticleType defaultParticleType, final ClientLevel clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
            return new GoParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}