package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import lombok.NonNull;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.attack.moves.AbstractCounterAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.effects.FlammableEffect;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.network.s2c.IPSTriggeredPacket;
import net.arna.jcraft.common.util.IJCraftComboTracker;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin_logic.LivingEntityMixinLogic;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IJCraftComboTracker {

    @Shadow protected int lastHurtByPlayerTime;
    @Shadow @Nullable protected Player lastHurtByPlayer;
    // Damage scaling
    @Unique
    private float jcraft$damageScaling = 1.00f;
    @Unique
    private int jcraft$hitCount = 0;
    // The relevant HashSets are lazy-loaded
    @Unique
    private final Map<LivingEntity, HashSet<MoveUsage>> jcraft$usedComboMoves = new HashMap<>();

    @Override
    public float jcraft$getDamageScaling() {
        return jcraft$damageScaling;
    }

    @Override
    public int jcraft$getHitCount() {
        return jcraft$hitCount;
    }

    @Override
    public void jcraft$increaseHitCount(final boolean tsHit) {
        jcraft$hitCount++;
        var minimum = JServerConfig.DAMAGE_SCALING_MINIMUM.getValue();
        var penalty = JServerConfig.SCALING_PENALTY_PER_HIT.getValue();

        if (tsHit) {
            minimum /= 2.0f;
            penalty *= 2.0f;
        }

        jcraft$damageScaling = Math.max(
                minimum,
                jcraft$damageScaling - penalty
        );
    }

    /**
     * @return Whether this move was present in the combo beforehand
     */
    @Override
    public boolean jcraft$addMoveToCombo(final @NonNull LivingEntity attacker, final MoveUsage moveUsage) {
        if (jcraft$usedComboMoves.containsKey(attacker)) {
            final AbstractMove<?, ?> move = moveUsage.move();
            final HashSet<MoveUsage> moveList = jcraft$usedComboMoves.get(attacker);

            for (MoveUsage pastUsage : moveList) {
                if (
                        moveUsage != pastUsage // Ensure the same move usage only adds to the move list once
                        && Attacks.prototypeMatch(pastUsage.move(), move) // Move equality check that doesn't use instances
                ) {
                    LivingEntity attackerUser = JUtils.getUserIfStand(attacker);

                    if (attackerUser instanceof ServerPlayer serverPlayer) {
                        IPSTriggeredPacket.send(serverPlayer);
                    }

                    return true;
                }
            }

            if (move.isLoopPrevention()) {
                moveList.add(moveUsage);
                return false;
            }
        } else {
            final AbstractMove<?, ?> move = moveUsage.move();
            final HashSet<MoveUsage> moveList = new HashSet<>(2);

            if (move.isLoopPrevention()) {
                moveList.add(moveUsage);
                jcraft$usedComboMoves.put(attacker, moveList);
            }
        }
        return false;
    }

    @Override
    public boolean jcraft$comboFromAttackerContains(final LivingEntity attacker, final AbstractMove<?, ?> move) {
        if (!jcraft$usedComboMoves.containsKey(attacker))
            return false;

        for (var moveUsage : jcraft$usedComboMoves.get(attacker)) {
            if (Attacks.prototypeMatch(moveUsage.move(), move)) return true;
        }

        return false;
    }

    @Override
    public void jcraft$resetCombo() {
        for (var entry : jcraft$usedComboMoves.entrySet()) {
            entry.getValue().clear();
        }
        jcraft$damageScaling = 1.00f;
        jcraft$hitCount = 0;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V", shift = At.Shift.AFTER))
    public void jcraft$tick(final CallbackInfo callbackInfo) {
        LivingEntity living = LivingEntity.class.cast(this);
        if (jcraft$hitCount > 0 && !living.hasEffect(JStatusRegistry.DAZED.get())) {
            ((IJCraftComboTracker)this).jcraft$resetCombo();
        }

        if (!living.level().isClientSide() && FlammableEffect.isFlammable(living)) {
            if (living.isOnFire() && living.getRemainingFireTicks() <= 1) {
                living.setRemainingFireTicks(20);
            }

            BlockPos pos = living.blockPosition();
            if (living.level().getBlockState(pos).is(BlockTags.FIRE)
                    || living.level().getBlockState(pos.below()).is(BlockTags.FIRE)) {
                living.setSecondsOnFire(2);
            }
        }
    }

    @WrapMethod(method = "setLastHurtMob")
    public void jcraft$onAttacking(final Entity entity, final Operation<Void> original) {
        if (!JUtils.isAffectedByTimeStop((LivingEntity)(Object)this)) {
            original.call(entity);
        }
    }

    // Inability to jump in specific circumstances
    @WrapMethod(method = "getJumpBoostPower")
    public float jcraft$getJumpBoostVelocityModifier(final Operation<Float> original) {
        if (!JUtils.canJump((LivingEntity)(Object)this)) {
            return -1f; // Nullify jump
        }
        return original.call();
    }

    @WrapMethod(method = "jumpFromGround")
    public void jcraft$jumpFromGround(final Operation<Void> original) {
        if (JUtils.canJump((LivingEntity)(Object)this)) {
            original.call();
        }
    }

    // Counter hook - Living entity
    @WrapMethod(method = "actuallyHurt")
    protected void jcraft$applyDamage(final DamageSource damageSource, final float damageAmount, final Operation<Void> original) {
        if (!AbstractCounterAttack.handleCounter((LivingEntity)(Object)this, damageSource, damageAmount)) {
            original.call(damageSource, damageAmount);
        }
    }

    // Living entities can't attack while stunned/enslaved/time erased thanks to this and an attack attribute nullifier
    @WrapMethod(method = "hasLineOfSight")
    public boolean jcraft$canSee(final Entity entity, final Operation<Boolean> original) {
        if (!jcraft$doChecks(entity, (LivingEntity)(Object)this)) {
            return original.call(entity);
        }
        return false;
    }

    @WrapMethod(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z")
    public boolean jcraft$canTarget(final LivingEntity target, final Operation<Boolean> original) {
        if (!jcraft$doChecks(target, (LivingEntity)(Object)this)) {
            return original.call(target);
        }
        return false;
    }

    // This is actually an implementation for players (mobs have their effect ticking properly stopped in TS), but PlayerEntity doesn't override this
    @WrapMethod(method = "tickEffects")
    protected void jcraft$tickStatusEffects(final Operation<Void> original) {
        final LivingEntity entity = (LivingEntity)(Object)this;
        if (JComponentPlatformUtils.getTimeStopData(entity).isEmpty() ||
                JComponentPlatformUtils.getTimeStopData(entity).get().getTicks() <= 0
        ) {
            original.call();
        }
    }

    /**
     * @return <code>true</code> if the checks succeeded
     */
    private static @Unique boolean jcraft$doChecks(Entity entity, LivingEntity livingEntity) {
        if (
                ((livingEntity.hasEffect(JStatusRegistry.DAZED.get()) && !JUtils.isBlocking(livingEntity))
                        || livingEntity.hasEffect(JStatusRegistry.KNOCKDOWN.get()))
                        && (!livingEntity.getType().is(JTagRegistry.CANNOT_BE_STUNNED))
        ) {
            return true;
        }
        if (entity.getFirstPassenger() instanceof KingCrimsonEntity kingCrimson && kingCrimson.getTETime() > 0) {
            return true;
        }
        if (JComponentPlatformUtils.getMiscData(livingEntity).getMaster() == entity) {
            return true;
        }

        return false;
    }

    @WrapMethod(method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V")
    protected void jcraft$dropFromLootTable(final DamageSource damageSource, final boolean hitByPlayer, final Operation<Void> original) {
        if (JComponentPlatformUtils.getMiscData((LivingEntity)(Object)this).getMaster() == null) {
            original.call(damageSource, hitByPlayer);
        }
    }

    @ModifyReturnValue(method = "canStandOnFluid(Lnet/minecraft/world/level/material/FluidState;)Z", at = @At("RETURN"))
    protected boolean jcraft$walkOnLiquid(final boolean original) {
        final LivingEntity living = (LivingEntity)(Object)this;
        return original || LivingEntityMixinLogic.canWalkOnLiquid(living.level(), living);
    }

    @ModifyReturnValue(method = "isAffectedByPotions()Z", at = @At("RETURN"))
    public boolean jcraft$dontApplyPotionsToTE(final boolean original) {
        return original && !JUtils.inTimeErase((LivingEntity)(Object)this);
    }

}
