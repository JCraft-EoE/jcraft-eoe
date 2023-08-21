package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Meant to be used as a temporary placeholder for moves when developing new stands/specs.
 * Doesn't do anything.
 * @param <A>
 */
public class DummyAttack<A extends IAttacker<?, ?>> extends AbstractMove<DummyAttack<A>, A> {
    private static final DummyAttack<IAttacker<?, ?>> instance = new DummyAttack<>();

    private DummyAttack() {
        super(0, 0, 0, 0);
    }

    public static <A extends IAttacker<?, ?>> DummyAttack<A> getInstance() {
        return (DummyAttack<A>) instance;
    }

    @Override
    public void onInitialize(A attacker) {}

    @Override
    public void tick(A attacker) {}

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    @NotNull
    @Override
    protected DummyAttack<A> getThis() {
        return this;
    }

    @NotNull
    @Override
    public DummyAttack<A> copy() {
        return this;
    }
}
