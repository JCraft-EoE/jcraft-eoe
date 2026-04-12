package net.arna.jcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OverlappingParticle extends RisingParticle {

    public OverlappingParticle(ClientLevel clientLevel, double x, double y, double z, double vx, double vy, double vz,
                               final SpriteSet spriteProvider) {
        super(clientLevel, x, y, z, vx, vy, vz);
        pickSprite(spriteProvider);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return JParticleTextureSheet.OVERLAP_SHEET;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(final SimpleParticleType parameters, final ClientLevel world, final double x,
                                       final double y, final double z, final double velocityX, final double velocityY, final double velocityZ) {
            return new OverlappingParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
        }
    }
}
