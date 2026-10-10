package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.arna.jcraft.api.attack.moves.AbstractCounterAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.attack.moves.hamon.ImproviserAttack;
import net.arna.jcraft.common.attack.moves.ranger.RangerRollMove;
import net.arna.jcraft.common.attack.moves.ranger.RangerSlideMove;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.food.IFoodData;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.IComboCounter;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin_logic.AbilitiesAddon;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements IComboCounter, IFoodData {

    @Shadow
    protected FoodData foodData;
    @Shadow
    @Final
    private Abilities abilities;
    // Combo tracking
    @Unique
    private int jcraft$comboCount = 1;
    @Unique
    private LivingEntity jcraft$lastAttacked;

    @Override
    public LivingEntity jcraft$getLastAttacked() {
        return jcraft$lastAttacked;
    }

    @Override
    public void jcraft$setLastAttacked(LivingEntity l) {
        jcraft$lastAttacked = l;
    }

    @Override
    public int jcraft$getComboCount() {
        return jcraft$comboCount;
    }

    @Override
    public void jcraft$setComboCount(int i) {
        jcraft$comboCount = i;
    }

    @Override
    public void jcraft$incrementComboCount() {
        jcraft$comboCount++;
    }

    @Override
    public FoodData getFoodData() {
        return foodData;
    }

    @WrapMethod(method = "canEat")
    private boolean jcraft$canEat(final boolean canAlwaysEat, final Operation<Boolean> original) {
        if (((Player)(Object)this).hasEffect(JStatusRegistry.DAZED.get())) {
            return false;
        }
        return original.call(canAlwaysEat);
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;touch(Lnet/minecraft/world/entity/Entity;)V"))
    public boolean jcraft$dontHandleTouchInTimestop(final Player player, final Entity entity) {
        // If the entity is timestopped, ignore the touch event
        return !JUtils.isAffectedByTimeStop(entity);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void jcraft$playerTickTail(CallbackInfo info) {
        final Player player = (Player) (Object) this;
        if (JUtils.isAffectedByTimeStop(player)) {
            return;
        }

        final JSpec<?, ?> spec = JComponentPlatformUtils.getSpecData(player).getSpec();
        if (spec != null) {
            spec.tickSpec();
        }

        if (jcraft$lastAttacked == null || !jcraft$lastAttacked.isAlive()) {
            return;
        }

        final LivingEntity attacker = jcraft$lastAttacked.getLastHurtByMob();
        if (
                attacker == null || attacker == player ||
                (attacker instanceof IOwnable ownableAttacker && ownableAttacker.getMaster() == player))
        {
            return;
        }
        jcraft$lastAttacked = null;
        jcraft$comboCount = 0;

        if (player instanceof ServerPlayer serverPlayer) {
            ComboCounterPacket.send(serverPlayer, 0, 1.00f);
        }
    }

    // KNOCKDOWN, poison and ranger mobility moves preventing pose updating
    @WrapMethod(method = "updatePlayerPose")
    public void jcraft$updatePose(final Operation<Void> original) {
        final Player player = (Player)(Object)this;
        if (!player.hasEffect(JStatusRegistry.KNOCKDOWN.get())
                        && !player.hasEffect(JStatusRegistry.WSPOISON.get())
                        && !jcraft$inRangerMobilityMove()
        ) {
            original.call();
        }
    }

    @Unique
    private boolean jcraft$inRangerMobilityMove() {
        final JSpec<?, ?> spec = JComponentPlatformUtils.getSpecData((Player)(Object)this).getSpec();
        if (spec == null || spec.moveStun <= 0) {
            return false;
        }
        // The roll releases its pose during recovery so it can blend back while the animation finishes
        if (spec.getCurrentMove() instanceof RangerRollMove) {
            return spec.moveStun > RangerRollMove.RECOVERY_TICKS;
        }
        return spec.getCurrentMove() instanceof RangerSlideMove;
    }

    // Can't M1/Light in TS or during spec moves, LivingEntity does not override this
    @WrapMethod(method = "attack")
    public void jcraft$attack(final Entity target, final Operation<Void> original) {
        final Player player = (Player)(Object)this;
        boolean cancel = false;
        if (JUtils.isAffectedByTimeStop(player)) {
            cancel = true;
        }

        // Can't M1/Light without a weapon while stand ON
        if (JUtils.getStand(player) != null && player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()) {
            cancel = true;
        }

        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec != null && spec.moveStun > 0) {
            cancel = true;
        }

        if (!cancel) {
            original.call(target);
        }
    }

    // Counter hook - player entity
    @WrapMethod(method = "actuallyHurt")
    protected void jcraft$applyDamage(final DamageSource damageSource, final float damageAmount, final Operation<Void> original) {
        if (!AbstractCounterAttack.handleCounter((Player)(Object)this, damageSource, damageAmount)) {
            original.call(damageSource, damageAmount);
        }
    }

    @WrapMethod(method = "jumpFromGround")
    public void jcraft$jumpFromGround(final Operation<Void> original) {
        if (JUtils.canJump((LivingEntity)(Object)this)) {
            original.call();
        }
    }

    @SuppressWarnings("ConstantValue")
    @WrapMethod(method = "startFallFlying")
    void jcraft$startFallFlying(final Operation<Void> original) {
        if (!JServerConfig.DISABLE_COMBAT_ELYTRA.getValue() || !JComponentPlatformUtils.getMiscData((Player)(Object)this).isOnDamageTimer()) {
            original.call();
        }
    }

    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(method = "isAffectedByFluids", at = @At("RETURN"))
    private boolean jcraft$unaffectedByFluidsIfCreaming(final boolean original) {
        return original && !CreamEntity.isCreaming((LivingEntity) (Object)this);
    }

    // Player.tick calls isSpectator() twice back-to-back: once to set noPhysics, and once to
    // gate setOnGround(false). We need BOTH to flip while creaming -- without the second one,
    // onGround stays true, getFrictionInfluencedSpeed takes the on-ground branch, and our
    // getFlyingSpeed override below is never reached for horizontal movement.
    @SuppressWarnings("ConstantValue")
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"))
    private boolean jcraft$noPhysicsIfCreaming(final boolean original) {
        return original || CreamEntity.isCreaming((LivingEntity) (Object)this);
    }

    @ModifyReturnValue(method = "getFlyingSpeed", at = @At("RETURN"))
    private float jcraft$overrideFlightSpeedIfCreaming(final float original) {
        return CreamEntity.isCreaming((LivingEntity) (Object)this) ? CreamEntity.VOIDING_FLIGHT_SPEED : original;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void jcraft$setAbilitiesPlayer(final Level level, final BlockPos pos, final float yRot, final GameProfile gameProfile, final CallbackInfo ci) {
        ((AbilitiesAddon) abilities).jcraft$setPlayer((Player) (Object) this);
    }

    @SuppressWarnings("ConstantValue")
    @WrapMethod(method = "attack(Lnet/minecraft/world/entity/Entity;)V")
    private void jcraft$substituteAttack(final Entity target, final Operation<Void> original) {
        if (!((Object)this instanceof ServerPlayer player) ||
                !(JUtils.getSpec(player) instanceof HamonSpec hamon) ||
                !(hamon.getCurrentMove() instanceof ImproviserAttack))
        {
            original.call(target);
        }
    }

}
