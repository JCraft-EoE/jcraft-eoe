package net.arna.jcraft.api.attack.core;

import com.google.common.base.MoreObjects;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.common.util.JUtils;

public abstract class MoveCondition<C extends MoveCondition<C, A>, A extends IAttacker<? extends A, ?>> {
    private Class<? extends A> attackerClass;

    public abstract boolean test(final A attacker);

    public abstract @NonNull MoveConditionType<C> getType();

    /**
     * Returns the class of the {@link A} type arg or the upper bound if not specified.
     * Used to check whether the moves added by data files are compatible with the attacker.
     * @return The class of the {@link A} type arg.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends A> getAttackerClass() {
        if (attackerClass == null) {
            // Default to IAttacker if somehow this is null.
            Class<?> resolvedClass = JUtils.resolveAttackerClass(MoveCondition.class, this);
            attackerClass = (Class<? extends A>) MoreObjects.firstNonNull(resolvedClass, IAttacker.class);
        }

        return attackerClass;
    }
}
