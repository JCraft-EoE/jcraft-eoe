package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

/**
 * Not really an attack, but rather a placeholder to indicate that you've
 * missed your counter and are punished for it.
 */
public class CounterMissAttack<S extends StandEntity<?, ?>> extends AbstractMove<CounterMissAttack<S>, S> {
    public CounterMissAttack(int duration) {
        super(0, duration + 1, duration, 1f);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    @Override
    protected @NonNull CounterMissAttack<S> getThis() {
        return this;
    }

    @Override
    public @NonNull CounterMissAttack<S> copy() {
        return new CounterMissAttack<>(getDuration());
    }
}
