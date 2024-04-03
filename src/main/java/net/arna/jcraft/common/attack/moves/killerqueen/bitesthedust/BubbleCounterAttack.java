package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;

public class BubbleCounterAttack extends AbstractCounterAttack<BubbleCounterAttack, KQBTDEntity> {
    private static final CounterMissMove<KQBTDEntity> missAttack = new CounterMissMove<>(15);

    public BubbleCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull KQBTDEntity attacker, @NonNull LivingEntity user) {
        attacker.setMove(missAttack, KQBTDEntity.State.COUNTER_MISS);
        StandEntity.stun(user, missAttack.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull KQBTDEntity attacker, Entity countered, DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);
        if (countered == null || !attacker.hasUser() || counteredDamageSource.isOf(DamageTypes.MAGIC)) return;

        if (countered instanceof LivingEntity livingEntity) {
            StandEntity.stun(livingEntity, 10, 3);
            JUtils.cancelMoves(livingEntity);
        }

        JComponents.getBombTracker(attacker.getUser()).getMainBomb().setBomb(countered);
        //stand.playSound(JSoundRegistry.BTD_COUNTER_HIT, 1, 1);
    }

    @Override
    protected @NonNull BubbleCounterAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BubbleCounterAttack copy() {
        return copyExtras(new BubbleCounterAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
