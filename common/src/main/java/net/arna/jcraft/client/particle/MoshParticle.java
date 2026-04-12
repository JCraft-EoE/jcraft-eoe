package net.arna.jcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3f;

public class MoshParticle extends TextureSheetParticle {
    protected final SpriteSet spriteProvider;

    MoshParticle(final ClientLevel world, final double x, final double y, final double z,
                 final SpriteSet spriteProvider, final Vector3f color) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;
        this.alpha = 1.00f;
        this.quadSize = 0.25f + random.nextFloat() * 0.25f;
        this.lifetime = 10 + random.nextInt(8);
        final float bright = 0.65f + random.nextFloat() * 0.35f;
        final float maxComp = Math.max(color.x(), Math.max(color.y(), color.z()));
        if (maxComp > 0.01f) {
            final float scale = bright / maxComp;
            this.setColor(color.x() * scale, color.y() * scale, color.z() * scale);
        } else {
            this.setColor(bright, bright, bright);
        }
        // Hover in place with a tiny upward drift
        this.xd = 0;
        this.yd = 0.005;
        this.zd = 0;
        this.gravity = 0;
        setSpriteFromAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getLightColor(final float tint) {
        return super.getLightColor(tint);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        public static Vector3f color = new Vector3f(0.7f, 0.7f, 0.7f);

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(final SimpleParticleType type, final ClientLevel world,
                                       final double x, final double y, final double z,
                                       final double vx, final double vy, final double vz) {
            return new MoshParticle(world, x, y, z, this.spriteProvider, color);
        }
    }
}
