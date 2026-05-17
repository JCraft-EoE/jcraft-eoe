package net.arna.jcraft.mixin;

import lombok.Getter;
import net.arna.jcraft.common.attack.moves.hamon.ImproviserMove;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @Getter
    @Unique
    private boolean jcraft$hamonized;

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    protected void jcraft$hamonize(final Entity owner, final CallbackInfo ci) {
        final Projectile projectile = (Projectile)(Object)this;
        if (projectile.tickCount == 0 && owner instanceof LivingEntity living &&
                JUtils.getSpec(living) instanceof HamonSpec hamon &&
                hamon.getCurrentMove() instanceof ImproviserMove) {
            jcraft$hamonized = true;
        }
    }

}
