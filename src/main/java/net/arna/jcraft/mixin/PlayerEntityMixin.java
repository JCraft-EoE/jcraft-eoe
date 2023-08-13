package net.arna.jcraft.mixin;

import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.IComboCounter;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IComboCounter {

    @Shadow
    public abstract void increaseStat(Stat<?> stat, int amount);

    // Combo tracking
    private int comboCount = 1;
    private LivingEntity lastAttacked;

    @Override
    public LivingEntity getLastAttacked() {
        return lastAttacked;
    }

    @Override
    public void setLastAttacked(LivingEntity l) {
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

    @Inject(at = @At("TAIL"), method = "tick")
    public void jcraft$playerTick(CallbackInfo info) {
        JCraftSpec spec = JComponents.getSpecData((PlayerEntity) (Object) this).getSpec();
        if (spec != null) spec.tickSpec();

        if (lastAttacked == null || !lastAttacked.isAlive()) return;

        LivingEntity attacker = lastAttacked.getAttacker();
        if (attacker == null || attacker == (Object) this) return;
        lastAttacked = null;
        comboCount = 0;

        //noinspection ConstantValue // Incorrect
        if ((Object) this instanceof ServerPlayerEntity serverPlayer)
            ComboCounterPacket.send(serverPlayer, 0, 1.00f);
    }

    // KNOCKDOWN and poison preventing pose updating
    @Inject(cancellable = true, at = @At("HEAD"), method = "updatePose")
    public void jcraft$updatePose(CallbackInfo info) {
        if (
                ((PlayerEntity) (Object) this).hasStatusEffect(JStatusRegistry.KNOCKDOWN)
                        || ((PlayerEntity) (Object) this).hasStatusEffect(JStatusRegistry.WSPOISON)
        ) {
            info.cancel();
        }
    }

    // Can't M1 in TS or during spec moves, LivingEntity does not override this
    @Inject(cancellable = true, method = "attack", at = @At("HEAD"))
    public void jcraft$attack(Entity target, CallbackInfo info) {
        if (JUtils.isAffectedByTimeStop((PlayerEntity) (Object) this)) info.cancel();

        JCraftSpec spec = JComponents.getSpecData((PlayerEntity) (Object) this).getSpec();
        if (spec != null && spec.moveStun > 0) info.cancel();
    }

    // Counter hook - player entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "applyDamage")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        PlayerEntity player = ((PlayerEntity) (Object) this);

        if (player.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
            Attack attack = stand.curAttack;
            if (attack != null) {
                if (attack.attackType == AttackType.COUNTER && stand.getMoveStun() < (attack.moveStun - attack.initTime)) {
                    stand.counter(source.getAttacker(), source); // Initiate counter
                    player.removeStatusEffect(JStatusRegistry.DAZED);
                    info.cancel();
                }
            }
        }
    }
}
