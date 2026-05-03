package net.arna.jcraft.mixin;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffect.class)
public abstract class MobEffectMixin {

    @Inject(method = "applyEffectTick(Lnet/minecraft/world/entity/LivingEntity;I)V", at = @At("HEAD"), cancellable = true)
    public void jcraft$dontTickEffectsInTE(final LivingEntity livingEntity, final int amplifier, final CallbackInfo ci) {
        if (JUtils.inTimeErase(livingEntity)) {
            ci.cancel();
        }
    }

}
