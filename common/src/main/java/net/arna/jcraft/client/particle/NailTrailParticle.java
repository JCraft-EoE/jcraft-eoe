package net.arna.jcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class NailTrailParticle extends TextureSheetParticle {
    private final SpriteSet spriteProvider;
    private static final float BASE_SPIN = (float) (Math.PI * 2);
    private static final float ANGULAR_ACCELERATION = 0.2f;
    private int frameIndex = 0;
    private float frameTicker = 0f;

    protected NailTrailParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;

        this.lifetime = 30;
        this.quadSize = 0.15f;

        this.rCol = 0.4f;
        this.gCol = 0.7f;
        this.bCol = 1.0f;

        this.alpha = 1.0f;
        this.gravity = 0.0f;

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.roll = this.random.nextFloat() * (float) (Math.PI * 2);
        this.oRoll = this.roll;

        this.setSprite(spriteProvider.get(0, 4));
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();

        float ageRatio = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0f - ageRatio;

        float angularVelocity = BASE_SPIN + ANGULAR_ACCELERATION * this.age;
        this.roll += angularVelocity;

        frameTicker += angularVelocity;
        if (frameTicker >= (float) (Math.PI / 2)) {
            frameTicker -= (float) (Math.PI / 2);
            frameIndex = (frameIndex + 1) % 4;
        }
        this.setSprite(spriteProvider.get(frameIndex, 4));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float tint) {
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
