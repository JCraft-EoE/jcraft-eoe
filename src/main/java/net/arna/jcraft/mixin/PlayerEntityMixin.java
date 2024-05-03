package net.arna.jcraft.mixin;

import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.IComboCounter;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IComboCounter {

    @Shadow
    public abstract void increaseStat(Stat<?> stat, int amount);

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

    @Inject(at = @At("TAIL"), method = "tick")
    public void jcraft$playerTickTail(CallbackInfo info) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (JUtils.isAffectedByTimeStop(player)) return;

        JSpec<?, ?> spec = JComponents.getSpecData(player).getSpec();
        if (spec != null) spec.tickSpec();

        if (lastAttacked == null || !lastAttacked.isAlive()) return;

        LivingEntity attacker = lastAttacked.getAttacker();
        if (attacker == null || attacker == player) return;
        lastAttacked = null;
        comboCount = 0;

        if (player instanceof ServerPlayerEntity serverPlayer)
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
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (JUtils.isAffectedByTimeStop(player)) info.cancel();

        // Can't M1 without a weapon while stand ON
        if (JUtils.getStand(player) != null && player.getMainHandStack().getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()) info.cancel();

        JSpec<?, ?> spec = JComponents.getSpecData(player).getSpec();
        if (spec != null && spec.moveStun > 0) info.cancel();
    }

    // Counter hook - player entity
    @Inject(cancellable = true, at = @At("HEAD"), method = "applyDamage")
    protected void jcraft$applyDamage(DamageSource source, float amount, CallbackInfo info) {
        PlayerEntity player = ((PlayerEntity) (Object) this);

        if (player.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
            AbstractMove<?, ?> attack = stand.curMove;
            if (attack == null || !attack.isCounter() || stand.getMoveStun() >= (attack.getDuration() - attack.getWindup()))
                return;

            //noinspection unchecked,rawtypes // Generic types can be annoying sometimes. This is fine.
            ((AbstractCounterAttack) attack).counter(stand, source.getAttacker(), source);
            //stand.counter(source.getAttacker(), source); // Initiate counter
            player.removeStatusEffect(JStatusRegistry.DAZED);
            info.cancel();
        }
    }
}
