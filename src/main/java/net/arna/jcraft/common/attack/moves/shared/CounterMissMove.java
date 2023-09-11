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
public class CounterMissMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<CounterMissMove<A>, A> {
    public CounterMissMove(int duration) {
        super(0, duration + 1, duration, 1f);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    @Override
    protected @NonNull CounterMissMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CounterMissMove<A> copy() {
        return copyExtras(new CounterMissMove<>(getDuration()));
    }
}
