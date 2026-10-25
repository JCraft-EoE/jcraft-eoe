package net.arna.jcraft.api.attack.core;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public abstract class MoveAction<T extends MoveAction<? extends T, A>, A extends IAttacker<? extends A, ?>> {
    @Getter
    @Setter
    private RunMoment runMoment = RunMoment.ON_STRIKE;
    private Class<? extends A> attackerClass;

    protected MoveAction() {}

    public abstract void perform(final A attacker, final LivingEntity user, final Set<LivingEntity> targets);

    public abstract @NonNull MoveActionType<T> getType();

    /**
     * Returns the class of the {@link A} type arg or the upper bound if not specified.
     * Used to check whether the moves added by data files are compatible with the attacker.
     * @return The class of the {@link A} type arg.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends A> getAttackerClass() {
        if (attackerClass == null) {
            // Default to IAttacker if somehow this is null.
            Class<?> resolvedClass = JUtils.resolveAttackerClass(MoveAction.class, this);
            attackerClass = (Class<? extends A>) MoreObjects.firstNonNull(resolvedClass, IAttacker.class);
        }

        return attackerClass;
    }

    /**
     * Sets the run moment of the given action to that of this action.
     * @param target The action whose run moment to set
     * @return The given action
     */
    protected T copyRunMoment(T target) {
        target.setRunMoment(runMoment);
        return target;
    }
}
