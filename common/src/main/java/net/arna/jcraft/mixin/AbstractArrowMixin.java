package net.arna.jcraft.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends ProjectileMixin {

    @Shadow
    public boolean inGround;

    @Override
    protected boolean jcraft$shouldDisplayHamonParticles() {
        return super.jcraft$shouldDisplayHamonParticles() && !inGround;
    }

}
