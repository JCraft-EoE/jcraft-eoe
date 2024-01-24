package net.arna.jcraft.client.particle;

import net.arna.jcraft.JCraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Vec3f;

public class AuraArcParticle extends AbstractSlowingParticle {
    protected final SpriteProvider spriteProvider;

    AuraArcParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, Vec3f color) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setColor(color.getX(), color.getY(), color.getZ());
        this.alpha = 0.4f;
        this.scale = 0.5f;
        this.maxAge = 7;
        setSpriteForAge(spriteProvider);
    }

    public void tick() {
        super.tick();
        setSpriteForAge(spriteProvider);
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
        public static Vec3f color = Vec3f.POSITIVE_X;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new AuraArcParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider, color);
        }
    }
}