package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.arna.jcraft.api.attack.moves.AbstractCounterAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.food.IFoodData;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.util.IComboCounter;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements IComboCounter, IFoodData {

    @Shadow protected FoodData foodData;
    // Combo tracking
    @Unique
    private int comboCount = 1;
    @Unique
    private LivingEntity lastAttacked;

    /*
    @Unique
    private boolean stunned = false;
    @Unique
    private int ticksSinceStun = 0;

    @Override
    public boolean jcraft$wasStunned() {
        return stunned;
    }
     */

    @Override
    public LivingEntity jcraft$getLastAttacked() {
        return lastAttacked;
    }

    @Override
    public void jcraft$setLastAttacked(LivingEntity l) {
        lastAttacked = l;
    }

    @Override
    public int jcraft$getComboCount() {
        return comboCount;
    }

    @Override
    public void jcraft$setComboCount(int i) {
        comboCount = i;
    }

    @Override
    public void jcraft$incrementComboCount() {
        comboCount++;
    }

    @Override
    public FoodData getFoodData() {
        return foodData;
    }

    /*
    @Inject(at = @At("HEAD"), method = "tick")
    public void jcraft$playerTickHead(CallbackInfo info) {
        if (lastAttacked == null) return;
        StatusEffectInstance stun = lastAttacked.getStatusEffect(JStatusRegistry.DAZED);
        boolean shouldBeStunned = stun != null && stun.getAmplifier() != 2;

        if (shouldBeStunned) {
            stunned = true;
            ticksSinceStun = 0;
        } else if (ticksSinceStun++ > 1) { // Intentional delay of 1 tick
            stunned = false;
        }
    }
     */

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;touch(Lnet/minecraft/world/entity/Entity;)V"))
    public boolean dontHandleTouchInTimestop(final Player player, final Entity entity) {
        // If the entity is timestopped, ignore the touch event
        return !JUtils.isAffectedByTimeStop(entity);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void jcraft$playerTickTail(CallbackInfo info) {
        Player player = (Player) (Object) this;
        if (JUtils.isAffectedByTimeStop(player)) {
            return;
        }

        JSpec<?, ?> spec = JComponentPlatformUtils.getSpecData(player).getSpec();
        if (spec != null) {
            spec.tickSpec();
        }

        if (lastAttacked == null || !lastAttacked.isAlive()) {
            return;
        }

        LivingEntity attacker = lastAttacked.getLastHurtByMob();
        if (attacker == null || attacker == player) {
            return;
        }
        lastAttacked = null;
        comboCount = 0;

        if (player instanceof ServerPlayer serverPlayer) {
            ComboCounterPacket.send(serverPlayer, 0, 1.00f);
        }
    }

    // KNOCKDOWN and poison preventing pose updating
    @Inject(cancellable = true, at = @At("HEAD"), method = "updatePlayerPose")
    public void jcraft$updatePose(CallbackInfo info) {
        if (
                ((Player) (Object) this).hasEffect(JStatusRegistry.KNOCKDOWN.get())
                        || ((Player) (Object) this).hasEffect(JStatusRegistry.WSPOISON.get())
        ) {
            info.cancel();
        }
    }

    // Can't M1/Light in TS or during spec moves, LivingEntity does not override this
    @Inject(cancellable = true, method = "attack", at = @At("HEAD"))
    public void jcraft$attack(Entity target, CallbackInfo info) {
        Player player = (Player) (Object) this;
        if (JUtils.isAffectedByTimeStop(player)) {
            info.cancel();
        }

        // Can't M1/Light without a weapon while stand ON
        if (JUtils.getStand(player) != null && player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()) {
            info.cancel();
        }

        JSpec<?, ?> spec = JComponentPlatformUtils.getSpecData(player).getSpec();
        if (spec != null && spec.moveStun > 0) {
            info.cancel();
        }
    }

    // Counter hook - player entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "actuallyHurt")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        Player player = ((Player) (Object) this);

        if (player.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
            AbstractMove<?, ?> attack = stand.getCurrentMove();
            if (attack == null || !attack.isCounter() || stand.getMoveStun() >= (attack.getDuration() - attack.getWindup())) {
                return;
            }

            //noinspection unchecked,rawtypes // Generic types can be annoying sometimes. This is fine.
            ((AbstractCounterAttack) attack).counter(stand, source.getEntity(), source);
            //stand.counter(source.getAttacker(), source); // Initiate counter
            player.removeEffect(JStatusRegistry.DAZED.get());
            info.cancel();
        }
    }

    @Inject(cancellable = true, method = "jumpFromGround", at = @At("HEAD"))
    public void jcraft$jumpFromGround(CallbackInfo ci) {
        LivingEntity entity = ((LivingEntity) (Object) this);
        if (!JUtils.canJump(entity)) {
            ci.cancel();
        }
    }

    @Inject(cancellable = true, at = @At("HEAD"), method = "startFallFlying")
    void jcraft$startFallFlying(CallbackInfo ci) {
        Player player = ((Player) (Object) this);
        if (JServerConfig.DISABLE_COMBAT_ELYTRA.getValue() && JComponentPlatformUtils.getMiscData(player).isOnDamageTimer()) {
            ci.cancel();
        }
    }
}
