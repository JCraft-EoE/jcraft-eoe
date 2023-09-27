package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class SCCounterAttack extends AbstractCounterAttack<SCCounterAttack, SilverChariotEntity> {
    private final CounterMissMove<SilverChariotEntity> counterMiss = new CounterMissMove<>(20);

    public SCCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull SilverChariotEntity attacker, @NonNull LivingEntity user) {
        attacker.setMove(counterMiss, SilverChariotEntity.State.COUNTER_MISS);
        StandEntity.stun(user, counterMiss.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull SilverChariotEntity attacker, Entity countered, DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);

        if (!(countered instanceof LivingEntity ent)) return;
        StandEntity.stun(ent, 30, 0);
        JUtils.cancelMoves(ent);
    }

    @Override
    protected @NonNull SCCounterAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SCCounterAttack copy() {
        return copyExtras(new SCCounterAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
