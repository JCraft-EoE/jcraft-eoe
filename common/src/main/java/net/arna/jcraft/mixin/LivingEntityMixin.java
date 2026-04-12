package net.arna.jcraft.mixin;

import lombok.NonNull;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.attack.moves.AbstractCounterAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.network.s2c.IPSTriggeredPacket;
import net.arna.jcraft.common.util.IJCraftComboTracker;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IJCraftComboTracker {
    @Shadow protected int lastHurtByPlayerTime;
    @Shadow @Nullable protected Player lastHurtByPlayer;
    // Damage scaling
    @Unique
    private float damageScaling = 1.00f;
    @Unique
    private int hitCount = 0;
    // The relevant HashSets are lazy-loaded
    @Unique
    private final Map<LivingEntity, HashSet<MoveUsage>> usedComboMoves = new HashMap<>();

    @Override
    public float jcraft$getDamageScaling() {
        return damageScaling;
    }

    @Override
    public int jcraft$getHitCount() {
        return hitCount;
    }

    @Override
    public void jcraft$increaseHitCount() {
        hitCount++;
        damageScaling = Math.max(
                JServerConfig.DAMAGE_SCALING_MINIMUM.getValue(),
                damageScaling - JServerConfig.SCALING_PENALTY_PER_HIT.getValue()
        );
    }

    /**
     * @return Whether this move was present in the combo beforehand
     */
    @Override
    public boolean jcraft$addMoveToCombo(@NonNull LivingEntity attacker, MoveUsage moveUsage) {
        if (usedComboMoves.containsKey(attacker)) {
            final AbstractMove<?, ?> move = moveUsage.move();
            final HashSet<MoveUsage> moveList = usedComboMoves.get(attacker);

            for (MoveUsage pastUsage : moveList) {
                if (
                        moveUsage != pastUsage // Ensure the same move usage only adds to the move list once
                        && Attacks.prototypeMatch(pastUsage.move(), move) // Move equality check that doesn't use instances
                ) { // TODO: verify prototypeMatch() filters appropriately
                    LivingEntity attackerUser = JUtils.getUserIfStand(attacker);

                    if (attackerUser instanceof ServerPlayer serverPlayer) {
                        IPSTriggeredPacket.send(serverPlayer);
                    }

                    return true;
                }
            }

            if (move.isLoopPrevention()) { // TODO: verify noLoopPrevention() is applied to all intended moves
                moveList.add(moveUsage);
                return false;
            }
        } else {
            final AbstractMove<?, ?> move = moveUsage.move();
            final HashSet<MoveUsage> moveList = new HashSet<>(2);

            if (move.isLoopPrevention()) {
                moveList.add(moveUsage);
                usedComboMoves.put(attacker, moveList);
            }
        }
        return false;
    }

    @Override
    public boolean jcraft$comboFromAttackerContains(LivingEntity attacker, AbstractMove<?, ?> move) {
        if (!usedComboMoves.containsKey(attacker))
            return false;

        for (var moveUsage : usedComboMoves.get(attacker)) {
            if (Attacks.prototypeMatch(moveUsage.move(), move)) return true;
        }

        return false;
    }

    @Override
    public void jcraft$resetCombo() {
        for (var entry : usedComboMoves.entrySet()) {
            entry.getValue().clear();
        }
        damageScaling = 1.00f;
        hitCount = 0;
    }

    // Called serverside if the LivingEntity wasn't removed
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V", shift = At.Shift.AFTER))
    public void jcraft$tick(CallbackInfo callbackInfo) {
        LivingEntity living = LivingEntity.class.cast(this);
        if (hitCount > 0 && !living.hasEffect(JStatusRegistry.DAZED.get())) {
            ((IJCraftComboTracker) this).jcraft$resetCombo();
        }
    }

    @Inject(cancellable = true, method = "setLastHurtMob", at = @At("HEAD"))
    public void jcraft$onAttacking(Entity target, CallbackInfo info) {
        if (JUtils.isAffectedByTimeStop((LivingEntity) (Object) this)) {
            info.cancel();
        }
    }

    // Inability to jump in specific circumstances
    @Inject(cancellable = true, method = "getJumpBoostPower", at = @At("HEAD"))
    public void jcraft$getJumpBoostVelocityModifier(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        if (!JUtils.canJump(entity)) {
            cir.setReturnValue(-1.0f); // Nullify jump
        }
        /*
        else if (stand != null && (stand.curAttack != null && stand.curAttack.attackType == AttackType.BARRAGE)) { // Stand ON and barraging
            cir.setReturnValue(-0.5D); // Reduce jump
        }
         */
    }

    @Inject(cancellable = true, method = "jumpFromGround", at = @At("HEAD"))
    public void jcraft$jumpFromGround(CallbackInfo ci) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        if (!JUtils.canJump(entity)) {
            ci.cancel();
        }
    }

    // Counter hook - Living entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "actuallyHurt")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        LivingEntity player = ((LivingEntity) (Object) this);

        if (!(player.getFirstPassenger() instanceof StandEntity<?, ?> stand)) {
            return;
        }
        AbstractMove<?, ?> attack = stand.getCurrentMove();
        if (attack == null || !attack.isCounter() || stand.getMoveStun() >= (attack.getDuration() - attack.getWindup())) {
            return;
        }

        //noinspection unchecked,rawtypes // Generic types can be annoying sometimes. This is fine.
        ((AbstractCounterAttack) attack).counter(stand, source.getEntity(), source);
//        stand.counter(source.getAttacker(), source); // Initiate counter
        player.removeEffect(JStatusRegistry.DAZED.get());
        info.cancel();
    }

    // Living entities can't attack while stunned/enslaved/time erased thanks to this and an attack attribute nullifier
    @Inject(cancellable = true, method = "hasLineOfSight", at = @At("HEAD"))
    public void jcraft$canSee(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        doChecks(entity, cir, livingEntity);
    }

    @Inject(cancellable = true, method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"))
    public void jcraft$canTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        doChecks(target, cir, (LivingEntity) (Object) this);
    }

    // This is actually an implementation for players (mobs have their effect ticking properly stopped in TS), but PlayerEntity doesn't override this
    @Inject(cancellable = true, at = @At("HEAD"), method = "tickEffects")
    protected void jcraft$tickStatusEffects(CallbackInfo ci) {
        if (JComponentPlatformUtils.getTimeStopData((LivingEntity) (Object) this).isPresent()) {
            if (JComponentPlatformUtils.getTimeStopData((LivingEntity) (Object) this).get().getTicks() > 0) {
                ci.cancel();
            }
        }
    }

    private static @Unique void doChecks(Entity entity, CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity) {
        if (
                ((livingEntity.hasEffect(JStatusRegistry.DAZED.get()) && !JUtils.isBlocking(livingEntity))
                        || livingEntity.hasEffect(JStatusRegistry.KNOCKDOWN.get()))
                        && (!livingEntity.getType().is(JTagRegistry.CANNOT_BE_STUNNED))
        ) {
            cir.setReturnValue(false);
        }

        if (entity.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson && kingCrimson.getTETime() > 0) {
            cir.setReturnValue(false);
        }

        if (JComponentPlatformUtils.getMiscData(livingEntity).getMaster() == entity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(cancellable = true, method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At("HEAD"))
    protected void jcraft$dropFromLootTable(final DamageSource damageSource, final boolean hitByPlayer, final CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (JComponentPlatformUtils.getMiscData(living).getMaster() != null) {
            ci.cancel();
        }
    }
}
