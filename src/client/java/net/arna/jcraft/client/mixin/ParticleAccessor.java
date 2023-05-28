package net.arna.jcraft.client.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("prevPosX")
    void setPrevX(double value);

    @Accessor("prevPosY")
    void setPrevY(double value);

    @Accessor("prevPosZ")
    void setPrevZ(double value);

    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();
}
