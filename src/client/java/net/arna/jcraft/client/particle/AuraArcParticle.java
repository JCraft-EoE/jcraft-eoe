package net.arna.jcraft.client.particle;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class AuraArcParticle extends AbstractSlowingParticle {
    protected final SpriteProvider spriteProvider;
    private final Entity parent;

    AuraArcParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, Vector3f color, Entity parent) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setColor(color.x(), color.y(), color.z());
        this.alpha = 0.4f;
        this.scale = 0.5f;
        this.maxAge = 7;
        this.parent = parent;
        tryMatchParent();
        setSpriteForAge(spriteProvider);
    }

    private void tryMatchParent() {
        if (parent != null) {
            Vec3d deltaPos = JUtils.deltaPos(parent);
            setVelocity(deltaPos.x, deltaPos.y, deltaPos.z);
        }
    }
    public void tick() {
        super.tick();
        tryMatchParent();
        setSpriteForAge(spriteProvider);
    }

    public ParticleTextureSheet getType() {
        return JParticleTextureSheet.PARTICLE_SHEET_AURA;
    }

    @Override
    protected int getBrightness(float tint) {
        return 255;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;
        public static Vector3f color = new Vector3f(1f ,0f,0f);
        public static Entity parent = null;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new AuraArcParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider, color, parent);
        }
    }
}