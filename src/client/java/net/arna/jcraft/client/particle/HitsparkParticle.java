package net.arna.jcraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class HitsparkParticle extends AbstractSlowingParticle {
    private final SpriteProvider spriteProvider;

    HitsparkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.alpha = 1f;
        this.scale = 0.5f;
        this.maxAge = 7;
        this.setSpriteForAge(spriteProvider);
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    protected int getBrightness(float tint) {
        return 255;
    }

    @Environment(EnvType.CLIENT)
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
