package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffect.class)
public abstract class MobEffectMixin {

    @WrapMethod(method = "applyEffectTick(Lnet/minecraft/world/entity/LivingEntity;I)V")
    public void jcraft$dontTickEffectsInTE(final LivingEntity livingEntity, final int amplifier, final Operation<Void> original) {
        if (!JUtils.inTimeErase(livingEntity)) {
            original.call(livingEntity, amplifier);
        }
    }

}
