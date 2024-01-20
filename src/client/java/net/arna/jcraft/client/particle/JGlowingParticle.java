package net.arna.jcraft.client.particle;

import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public class JGlowingParticle extends AbstractSlowingParticle {
    protected final SpriteProvider spriteProvider;

    JGlowingParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        initialize();
        this.setSpriteForAge(spriteProvider);
    }

    protected void initialize() {

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
}
