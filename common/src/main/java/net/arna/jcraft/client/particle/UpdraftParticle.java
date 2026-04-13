package net.arna.jcraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class UpdraftParticle extends JGlowingParticle {

    UpdraftParticle(final ClientLevel world, final double x, final double y, final double z,
                    final double velocityX, final double velocityY, final double velocityZ,
                    final SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
    }

    @Override
    protected void initialize() {
        this.lifetime = 20;
        this.quadSize = 1.2f;
        this.alpha = 0.75f;
        // Small upward drift on top of whatever velocity was passed in
        this.yd += 0.04;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(final SimpleParticleType type, final ClientLevel world,
                                       final double x, final double y, final double z,
                                       final double vx, final double vy, final double vz) {
            return new UpdraftParticle(world, x, y, z, vx, vy, vz, this.spriteProvider);
        }
    }
}
