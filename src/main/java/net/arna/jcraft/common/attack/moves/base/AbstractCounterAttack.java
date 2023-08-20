package net.arna.jcraft.common.attack.moves.base;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

import java.util.Set;

public abstract class AbstractCounterAttack<T extends AbstractCounterAttack<T, S>, S extends StandEntity<?, ?>> extends AbstractMove<T, S> {
    protected AbstractCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        counter = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    /**
     * Called when this counter-attack missed.
     * Typically, sets the attack to a counter miss attack and stuns the user.
     * @param stand The stand that missed
     * @param user The stand's user
     */
    public abstract void whiff(@NonNull S stand, @NonNull LivingEntity user);

    /**
     * Called when this counter-attack hit.
     * Typically, resets the user's attack, but can also somehow reward them for it.
     * @param stand The stand that hit
     * @param countered The entity whose attack was countered
     * @param counteredDamageSource The damage source the countered entity was using
     */
    public void counter(@NonNull S stand, Entity countered, DamageSource counteredDamageSource) {
        stand.setMoveStun(0);
        stand.curMove = null;
    }
}
