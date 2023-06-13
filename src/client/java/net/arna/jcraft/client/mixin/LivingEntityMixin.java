package net.arna.jcraft.client.mixin;

import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // Inability to jump during hitstun and knockdown
    @Inject(cancellable = true, method = "getJumpBoostVelocityModifier", at = @At("HEAD"))
    public void jcraft$getJumpBoostVelocityModifier(CallbackInfoReturnable<Double> cir) {
        LivingEntity player = ((LivingEntity) (Object) this);
        StatusEffectInstance stun = player.getStatusEffect(JStatusRegister.DAZED);
        if (
                player.hasStatusEffect(JStatusRegister.KNOCKDOWN)
                        || (stun != null && stun.getAmplifier() != 2)
                        || player.getFirstPassenger() instanceof StandEntity stand && stand.getRemote()) {
            cir.setReturnValue(-1.0D);
        }
    }
}
