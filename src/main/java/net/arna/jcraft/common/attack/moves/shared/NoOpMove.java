package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

/**
 * A move that doesn't do anything.
 * Meant to be used either as a temporary placeholder when developing stands or specs or
 * for moves that don't really do anything apart from what's done in the initMove method
 * in either the stand or the spec.
 * @param <A>
 */
public class NoOpMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<NoOpMove<A>, A> {
    public NoOpMove() {
        this(0, 0, 0f);
    }

    public NoOpMove(int cooldown, int duration, float moveDistance) {
        super(cooldown, duration + 1, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    @Override
    protected @NonNull NoOpMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull NoOpMove<A> copy() {
        return copyExtras(new NoOpMove<>(getCooldown(), getDuration(), getMoveDistance()));
    }
}
