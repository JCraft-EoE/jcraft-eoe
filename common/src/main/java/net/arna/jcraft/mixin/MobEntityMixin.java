package net.arna.jcraft.mixin;

import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void jcraft$dontDisableAI(CallbackInfoReturnable<LivingEntity> cir) {
        if (JComponentPlatformUtils.getStandComponent(Mob.class.cast(this)).getStand() != null)
            cir.setReturnValue(null);
    }
}
