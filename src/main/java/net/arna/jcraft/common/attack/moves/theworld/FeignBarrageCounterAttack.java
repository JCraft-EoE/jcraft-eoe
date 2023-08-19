package net.arna.jcraft.common.attack.moves.theworld;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheWorldEntity;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

public class FeignBarrageCounterAttack extends AbstractCounterAttack<FeignBarrageCounterAttack, TheWorldEntity> {
    private static final CounterMissAttack<TheWorldEntity> missAttack = new CounterMissAttack<>(10);

    public FeignBarrageCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull TheWorldEntity stand, @NonNull LivingEntity user) {
        stand.setAttack(missAttack, TheWorldEntity.State.COUNTER_MISS);
        StandEntity.stun(user, missAttack.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull TheWorldEntity stand, Entity countered, DamageSource counteredDamageSource) {
        super.counter(stand, countered, counteredDamageSource);

        if (countered == null || !stand.hasUser()) return;
        LivingEntity user = stand.getUserOrThrow();
        Vec3d behind = countered.getPos().subtract(countered.getRotationVector());

        user.setVelocity(0, 0, 0);
        user.velocityModified = true;
        user.teleport(behind.x, behind.y, behind.z);
        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, countered.getEyePos());

        if (countered instanceof LivingEntity livingEntity) {
            livingEntity.removeStatusEffect(JStatusRegistry.DAZED);
            StandEntity.stun(livingEntity, 20, 0);
            if (countered.getFirstPassenger() instanceof StandEntity<?, ?> counteredStand)
                counteredStand.cancelAttack();
        }

        stand.setAttack(TheWorldEntity.COUNTER_FOLLOWUP, TheWorldEntity.State.COUNTER_HIT);
        stand.playSound(JSoundRegistry.TIME_SKIP, 1, 1);
    }

    @Override
    protected FeignBarrageCounterAttack getThis() {
        return this;
    }

    @Override
    public FeignBarrageCounterAttack copy() {
        return new FeignBarrageCounterAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance());
    }
}
