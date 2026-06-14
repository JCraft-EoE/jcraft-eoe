package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.effects.ExhaustionEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Unique
    private final Map<UUID, Integer> jcraft$healCounter = new HashMap<>();

    @WrapOperation(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private void jcraft$increaseHealCounter(final Player instance, final float v, final Operation<Void> original) {
        original.call(instance, v);
        jcraft$healCounter.put(instance.getUUID(), 1 + jcraft$healCounter.getOrDefault(instance.getUUID(), 0));
    }

    @ModifyConstant(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", constant = @Constant(intValue = 10, ordinal = 0))
    public int jcraft$reduceFastFoodRegen(final int constant, @Local(argsOnly = true) Player player) {
        return jcraft$reduceFoodRegen(constant, player);
    }

    @ModifyConstant(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", constant = @Constant(intValue = 80, ordinal = 0))
    public int jcraft$reduceSlowFoodRegen(final int constant, @Local(argsOnly = true) Player player) {
        return jcraft$reduceFoodRegen(constant, player);
    }

    @Unique
    private int jcraft$reduceFoodRegen(final int constant, Player player) {
        final MobEffectInstance exhaustion = player.getEffect(JStatusRegistry.EXHAUSTION.get());
        if (exhaustion != null) {
            final int heals = jcraft$healCounter.getOrDefault(player.getUUID(), 0);
            if (heals % ExhaustionEffect.MAX_LEVEL > exhaustion.getAmplifier()) {
                return constant;
            }
            if (exhaustion.getAmplifier() == 0) {
                return constant * 2;
            }
            if (exhaustion.getAmplifier() == 1) {
                return constant * 3;
            }
            if (exhaustion.getAmplifier() == 2) {
                return constant * 4;
            }
            return Integer.MAX_VALUE;
        }
        return constant;
    }

}
