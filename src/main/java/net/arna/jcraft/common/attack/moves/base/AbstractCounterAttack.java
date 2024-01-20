package net.arna.jcraft.common.attack.moves.base;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.Set;

public abstract class AbstractCounterAttack<T extends AbstractCounterAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractMove<T, A> {
    protected AbstractCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        counter = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        if (attacker instanceof StandEntity<?,?> stand && stand.isFree())
            stand.setFree(false);
        return Set.of();
    }

    // TODO these implementations are almost always pretty much the same
    // We should probably make a default implementation for this instead.
    /**
     * Called when this counter-attack missed.
     * Typically, sets the attack to a counter miss attack and stuns the user.
     * @param attacker The stand that missed
     * @param user The stand's user
     */
    public abstract void whiff(@NonNull A attacker, @NonNull LivingEntity user);

    /**
     * Called when this counter-attack hit.
     * Typically, resets the user's attack, but can also somehow reward them for it.
     * @param attacker The stand that hit
     * @param countered The entity whose attack was countered
     * @param counteredDamageSource The damage source the countered entity was using
     */
    public void counter(@NonNull A attacker, Entity countered, DamageSource counteredDamageSource) {
        attacker.setMoveStun(0);
        attacker.setCurrentMove(null);
    }
}
