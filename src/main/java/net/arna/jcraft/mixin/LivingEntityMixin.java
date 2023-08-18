package net.arna.jcraft.mixin;

import net.arna.jcraft.common.attack.core.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.core.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.MiscComponent;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.IDamageScaler;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IDamageScaler {
    // Damage scaling
    @Unique
    private float damageScaling = 1.00f;
    @Unique
    private int hitCount = 0;
    @Override
    public float jcraft$getDamageScaling() {
        return this.damageScaling;
    }
    @Override
    public int jcraft$getHitCount() {
        return this.hitCount;
    }
    @Override
    public void jcraft$increaseHitCount() {
        hitCount++;
        if (damageScaling > 0.42f)
            damageScaling -= 0.02f;
    }
    @Override
    public void jcraft$resetHitCount() {
        damageScaling = 1.00f;
        hitCount = 0;
    }

    // Called serverside, if the LivingEntity wasn't removed
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickMovement()V", shift = At.Shift.AFTER))
    public void jcraft$tick(CallbackInfo callbackInfo) {
        LivingEntity living = LivingEntity.class.cast(this);
        if ( hitCount > 0 && !living.hasStatusEffect(JStatusRegistry.DAZED) ) {
            ((IDamageScaler) this).jcraft$resetHitCount();
        }
    }

    // Make stand users rideable entities in water (prevents stand desummon)
    @Inject(cancellable = true, method = "canBeRiddenInWater", at = @At("HEAD"))
    public void jcraft$canBeRiddenInWater(CallbackInfoReturnable<Boolean> cir) {
        if (JComponents.getStandData((LivingEntity) (Object) this).getType() != null)
            cir.setReturnValue(true);
    }

    @Inject(cancellable = true, method = "onAttacking", at = @At("HEAD"))
    public void jcraft$onAttacking(Entity target, CallbackInfo info) {
        if (JUtils.isAffectedByTimeStop((LivingEntity) (Object) this)) info.cancel();
    }

    // Inability to jump in specific circumstances
    @Inject(cancellable = true, method = "getJumpBoostVelocityModifier", at = @At("HEAD"))
    public void jcraft$getJumpBoostVelocityModifier(CallbackInfoReturnable<Double> cir) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        StandEntity<?, ?> stand = JUtils.getStand(entity);
        StatusEffectInstance stun = entity.getStatusEffect(JStatusRegistry.DAZED);
        if (
                entity.hasStatusEffect(JStatusRegistry.KNOCKDOWN) || // Knocked down
                        (stun != null && stun.getAmplifier() != 2) || // Stunned (not blocking)
                        (stand != null && stand.getRemote()) // Stand ON in remote mode
        ) cir.setReturnValue(-1.0D); // Nullify jump
        /*
        else if (stand != null && (stand.curAttack != null && stand.curAttack.attackType == AttackType.BARRAGE)) { // Stand ON and barraging
            cir.setReturnValue(-0.5D); // Reduce jump
        }
         */
    }

    // Counter hook - Living entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "applyDamage")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        LivingEntity player = ((LivingEntity) (Object) this);

        if (!(player.getFirstPassenger() instanceof StandEntity<?, ?> stand)) return;
        AbstractMove<?, ?> attack = stand.curAttack;
        if (attack == null || !attack.isCounter() || stand.getMoveStun() >= (attack.getMoveStun() - attack.getWindup()))
            return;

        //noinspection unchecked,rawtypes // Generic types can be annoying sometimes. This is fine.
        ((AbstractCounterAttack) attack).counter(stand, source.getAttacker(), source);
//        stand.counter(source.getAttacker(), source); // Initiate counter
        player.removeStatusEffect(JStatusRegistry.DAZED);
        info.cancel();
    }

    // Living entities can't attack while stunned/enslaved/time erased thanks to this and an attack attribute nullifier
    @Inject(cancellable = true, method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    public void jcraft$canSee(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        doChecks(entity, cir, livingEntity);
    }

    @Inject(cancellable = true, method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"))
    public void jcraft$canTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        doChecks(target, cir, (LivingEntity) (Object) this);
    }

    private static @Unique void doChecks(Entity entity, CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity) {
        if ((livingEntity.hasStatusEffect(JStatusRegistry.DAZED) && !JUtils.isBlocking(livingEntity)) ||
                livingEntity.hasStatusEffect(JStatusRegistry.KNOCKDOWN))
            cir.setReturnValue(false);

        if (entity.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson && kingCrimson.getTETime() > 0)
            cir.setReturnValue(false);

        MiscComponent miscData = JComponents.getMiscData(livingEntity);
        if (miscData.getSlavedTo() != null && miscData.getSlavedTo().equals(livingEntity.getUuid()))
            cir.setReturnValue(false);
    }
}
