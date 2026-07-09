package net.arna.jcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class LemonParticle extends RisingParticle { // literally just pixel particle code
    LemonParticle(final ClientLevel world, final double x, final double y, final double z, final double velocityX, final double velocityY, final double velocityZ, final SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.setSpriteFromAge(spriteProvider);
        this.quadSize = 0.1f;
        this.lifetime = 7;
        this.gravity = 0f;
        this.hasPhysics = true;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public void tick() {
        super.tick();
    }

    @Override
    protected int getLightColor(final float tint) {
        return 255;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(final SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(final @NotNull SimpleParticleType defaultParticleType, final @NotNull ClientLevel clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
            return new LemonParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
