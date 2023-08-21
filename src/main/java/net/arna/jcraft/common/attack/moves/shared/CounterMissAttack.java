package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

/**
 * Not really an attack, but rather a placeholder to indicate that you've
 * missed your counter and are punished for it.
 */
public class CounterMissAttack<A extends IAttacker<?, ?>> extends AbstractMove<CounterMissAttack<A>, A> {
    public CounterMissAttack(int duration) {
        super(0, duration + 1, duration, 1f);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    @Override
    protected @NonNull CounterMissAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CounterMissAttack<A> copy() {
        return new CounterMissAttack<>(getDuration());
    }
}
