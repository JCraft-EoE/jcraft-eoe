package net.arna.jcraft.api.attack.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.IAttacker;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
@Setter
public abstract class MoveAction<T extends MoveAction<? extends T, A>, A extends IAttacker<? extends A, ?>> {
    private RunMoment runMoment = RunMoment.ON_STRIKE;

    protected MoveAction() {}

    public abstract void perform(final A attacker, final LivingEntity user, final Set<LivingEntity> targets);

    public abstract @NonNull MoveActionType<T> getType();

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
