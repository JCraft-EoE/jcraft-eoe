package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // Make stand users rideable entities in water (prevents stand desummon)
    @Inject(cancellable = true, method = "canBeRiddenInWater", at = @At("HEAD"))
    public void jcraft$canBeRiddenInWater(CallbackInfoReturnable<Boolean> cir) {
        if (((IEntityDataSaver) this).getPersistentData().contains("StandID")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(cancellable = true, method = "onAttacking", at = @At("HEAD"))
    public void jcraft$onAttacking(Entity target, CallbackInfo info) {
        if (((ITimeStop) this).getTimeStopTicks() > 0) {
            info.cancel();
        }
    }

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

    // Counter hook - Living entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "applyDamage")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        LivingEntity player = ((LivingEntity) (Object) this);

        if (player.getFirstPassenger() instanceof StandEntity stand) {
            Attack attack = stand.curAttack;
            if (attack != null) {
                if (attack.attackType == AttackType.COUNTER && stand.getMoveStun() < (attack.moveStun - attack.initTime)) {
                    stand.counter(source.getAttacker(), source); // Initiate counter
                    player.removeStatusEffect(JStatusRegister.DAZED);
                    info.cancel();
                }
            }
        }
    }

    // Living entities can't attack while stunned/enslaved/time erased thanks to this and an attack attribute nullifier
    @Inject(cancellable = true, method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    public void jcraft$canSee(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        IEntityDataSaver entityDataSaver = (IEntityDataSaver) livingEntity;

        if ((livingEntity.hasStatusEffect(JStatusRegister.DAZED) && !JCraftUtils.isBlocking(livingEntity)) || livingEntity.hasStatusEffect(JStatusRegister.KNOCKDOWN)) {
            cir.setReturnValue(false);
        }

        if (entity.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson) {
            if (kingCrimson.getTETime() > 0) {
                cir.setReturnValue(false);
            }
        }

        if (entityDataSaver.getPersistentData().contains("SlavedTo")) {
            if (entityDataSaver.getPersistentData().getUuid("SlavedTo").equals(entity.getUuid())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(cancellable = true, method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"))
    public void jcraft$canTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        IEntityDataSaver entityDataSaver = (IEntityDataSaver) livingEntity;
        StatusEffectInstance stun = livingEntity.getStatusEffect(JStatusRegister.DAZED);

        if ((livingEntity.hasStatusEffect(JStatusRegister.DAZED) && !JCraftUtils.isBlocking(livingEntity)) || livingEntity.hasStatusEffect(JStatusRegister.KNOCKDOWN)) {
            cir.setReturnValue(false);
        }

        if (target.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson) {
            if (kingCrimson.getTETime() > 0) {
                cir.setReturnValue(false);
            }
        }

        if (entityDataSaver.getPersistentData().contains("SlavedTo")) {
            if (entityDataSaver.getPersistentData().getUuid("SlavedTo").equals(target.getUuid())) {
                cir.setReturnValue(false);
            }
        }
    }
}
