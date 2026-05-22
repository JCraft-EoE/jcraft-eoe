package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.mixin_logic.LivingEntityMixinLogic;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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

    @WrapOperation(method = "doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean jcraft$hamonAfterEffect(final Entity target, final DamageSource source, final float amount, final Operation<Boolean> original) {
        final boolean result = original.call(target, source, amount);
        if (result) {
            LivingEntityMixinLogic.hamonAfterEffect((LivingEntity)(Object) this, target);
        }
        return result;
    }
}
