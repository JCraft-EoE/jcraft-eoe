package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.effects.ExhaustionEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin {

    @Shadow
    @Final
    private MobEffect effect;

    @Unique
    private int jcraft$tickCount;

    @ModifyExpressionValue(method = "tick(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/Runnable;)Z", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;isDurationEffectTick(II)Z", ordinal = 0))
    public boolean jcraft$applyExhaustion(final boolean original, final @Local(argsOnly = true) LivingEntity living) {
        final MobEffectInstance exhaustion = living.getEffect(JStatusRegistry.EXHAUSTION.get());
        if (exhaustion != null && effect == MobEffects.REGENERATION && original) {
            return jcraft$tickCount++ % ExhaustionEffect.MAX_LEVEL > exhaustion.getAmplifier();
        }
        return original;
    }

}
