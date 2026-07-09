package net.arna.jcraft.client.particle;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AuraBlobParticle extends RisingParticle {
    protected final SpriteSet spriteProvider;
    private final Entity parent;

    AuraBlobParticle(final ClientLevel world, final double x, final double y, final double z, final double velocityX, final double velocityY, final double velocityZ, final SpriteSet spriteProvider, final Vector3f color, final Entity parent) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setColor(color.x(), color.y(), color.z());
        this.alpha = 0.25f;
        this.quadSize = 0.5f;
        this.lifetime = 6 + random.nextInt(6);
        this.parent = parent;
        setSpriteFromAge(spriteProvider);
    }

    private void tryMatchParent() {
        if (parent != null) {
            Vec3 deltaPos = JUtils.deltaPos(parent);
            setParticleSpeed(deltaPos.x, deltaPos.y, deltaPos.z);
        }
    }

    public void tick() {
        tryMatchParent();
        super.tick();
        if (age % 3 == 0) {
            setSprite(spriteProvider.get(random));
        }
    }

    public ParticleRenderType getRenderType() {
        return JParticleTextureSheet.PARTICLE_SHEET_AURA;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        public static Vector3f color = new Vector3f(1f, 0f, 0f);
        public static Entity parent = null;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(final SimpleParticleType defaultParticleType, final ClientLevel clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
            var out = new AuraBlobParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider, color, parent);
            out.tryMatchParent();
            return out;
        }
    }
}