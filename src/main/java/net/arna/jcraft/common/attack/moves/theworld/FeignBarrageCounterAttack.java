package net.arna.jcraft.common.attack.moves.theworld;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheWorldEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

public class FeignBarrageCounterAttack extends AbstractCounterAttack<FeignBarrageCounterAttack, TheWorldEntity> {
    private static final CounterMissMove<TheWorldEntity> missAttack = new CounterMissMove<>(10);
    private final AbstractMove<?, ? super TheWorldEntity> hitMove;

    public FeignBarrageCounterAttack(int cooldown, int windup, int duration, float moveDistance, AbstractMove<?, ? super TheWorldEntity> hitMove) {
        super(cooldown, windup, duration, moveDistance);
        this.hitMove = hitMove;
    }

    @Override
    public void whiff(@NonNull TheWorldEntity attacker, @NonNull LivingEntity user) {
        attacker.setMove(missAttack, TheWorldEntity.State.COUNTER_MISS);
        StandEntity.stun(user, missAttack.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull TheWorldEntity attacker, Entity countered, DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);

        if (countered == null || !attacker.hasUser()) return;
        LivingEntity user = attacker.getUserOrThrow();
        Vec3d behind = countered.getPos().subtract(countered.getRotationVector());

        user.setVelocity(0, 0, 0);
        user.velocityModified = true;
        user.teleport(behind.x, behind.y, behind.z);
        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, countered.getEyePos());

        if (countered instanceof LivingEntity livingEntity) {
            livingEntity.removeStatusEffect(JStatusRegistry.DAZED);
            StandEntity.stun(livingEntity, 20, 0);

            JUtils.cancelMoves(livingEntity);
        }

        attacker.setMove(hitMove, TheWorldEntity.State.COUNTER_HIT);
        attacker.playSound(JSoundRegistry.TIME_SKIP, 1, 1);
    }

    @Override
    protected @NonNull FeignBarrageCounterAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FeignBarrageCounterAttack copy() {
        return copyExtras(new FeignBarrageCounterAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), hitMove));
    }
}
