package net.arna.jcraft.client.particle;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Vec3d;

public class PurpleHazeErraticParticle extends AbstractSlowingParticle {
    protected PurpleHazeErraticParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, SpriteProvider spriteProvider) {
        super(clientWorld, d, e, f, g, h, i);
        this.maxAge = 16;
        this.scale = 0.05f;
        this.setColor(0.6f, 0.3f, 0.8f);
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d velocity = new Vec3d(
                velocityX,
                velocityY,
                velocityZ
        );

        velocity = velocity.add(JUtils.randUnitVec(random)).multiply(0.5);

        this.setVelocity(velocity.x, velocity.y, velocity.z);
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
            return new PurpleHazeErraticParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
