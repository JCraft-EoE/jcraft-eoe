package net.arna.jcraft.mixin;

import net.arna.jcraft.effects.ModStatusRegister;
import net.arna.jcraft.entity.KingCrimsonEntity;
import net.arna.jcraft.entity.StandEntity;
import net.arna.jcraft.util.*;
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
public abstract class JCraftLivingEntityMixin {
    // Make stand users rideable entities in water (prevents stand desummon)
    @Inject(cancellable = true, method = "canBeRiddenInWater", at = @At("HEAD"))
    public void jcraft$canBeRiddenInWater(CallbackInfoReturnable info) {
        if ( ((IEntityDataSaver)this).getPersistentData().contains("StandID") ) {
            info.setReturnValue(true);
        }
    }

    @Inject(cancellable = true, method = "onAttacking", at = @At("HEAD"))
    public void jcraft$onAttacking(Entity target, CallbackInfo info) {
        if ( ((ITimeStop)this).getTimeStopTicks() > 0) { info.cancel(); }
    }

    // Inability to jump during hitstun and knockdown
    @Inject(cancellable = true, method = "getJumpBoostVelocityModifier", at = @At("HEAD"))
    public void jcraft$getJumpBoostVelocityModifier(CallbackInfoReturnable info) {
        LivingEntity player = ((LivingEntity)(Object)this);
        StatusEffectInstance stun = player.getStatusEffect(ModStatusRegister.Dazed);
        if (
                player.hasStatusEffect(ModStatusRegister.Knockdown)
                || (stun != null && stun.getAmplifier() < 2)
                || player.getFirstPassenger() instanceof StandEntity stand && stand.getRemote())
        { info.setReturnValue(-1.0D); }
    }

    // Counter hook - Living entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "applyDamage")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        LivingEntity player = ((LivingEntity)(Object)this);

        if ( player.getFirstPassenger() instanceof StandEntity stand ) {
            Attack attack = stand.curAttack;
            if (attack != null) {
                if (attack.attackType == AttackType.COUNTER && stand.getMoveStun() < (attack.moveStun - attack.initTime)) {
                    stand.Counter(source.getAttacker(), source); // Initiate counter
                    player.removeStatusEffect(ModStatusRegister.Dazed);
                    info.cancel();
                }
            }
        }
    }

    // Living entities can't attack while stunned/enslaved/time erased thanks to this and an attack attribute nullifier
    @Inject(cancellable = true, method = "Lnet/minecraft/entity/LivingEntity;canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    public void jcraft$canSee(Entity entity, CallbackInfoReturnable info) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        IEntityDataSaver entityDataSaver = (IEntityDataSaver) livingEntity;

        if ( (livingEntity.hasStatusEffect(ModStatusRegister.Dazed) && !JCraftUtils.isBlocking(livingEntity)) || livingEntity.hasStatusEffect(ModStatusRegister.Knockdown)) {
            info.setReturnValue(false);
        }

        if (entity.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson) {
            if (kingCrimson.getTETime() > 0) {
                info.setReturnValue(false);
            }
        }

        if (entityDataSaver.getPersistentData().contains("SlavedTo")) {
            if (entityDataSaver.getPersistentData().getUuid("SlavedTo").equals(entity.getUuid())) {
                info.setReturnValue(false);
            }
        }
    }

    @Inject(cancellable = true, method = "Lnet/minecraft/entity/LivingEntity;canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"))
    public void jcraft$canTarget(LivingEntity entity, CallbackInfoReturnable info) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        IEntityDataSaver entityDataSaver = (IEntityDataSaver) livingEntity;
        StatusEffectInstance stun = livingEntity.getStatusEffect(ModStatusRegister.Dazed);

        if ( (livingEntity.hasStatusEffect(ModStatusRegister.Dazed) && !JCraftUtils.isBlocking(livingEntity)) || livingEntity.hasStatusEffect(ModStatusRegister.Knockdown)) {
            info.setReturnValue(false);
        }

        if (entity.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson) {
            if (kingCrimson.getTETime() > 0) {
                info.setReturnValue(false);
            }
        }

        if (entityDataSaver.getPersistentData().contains("SlavedTo")) {
            if (entityDataSaver.getPersistentData().getUuid("SlavedTo").equals(entity.getUuid())) {
                info.setReturnValue(false);
            }
        }
    }
}
