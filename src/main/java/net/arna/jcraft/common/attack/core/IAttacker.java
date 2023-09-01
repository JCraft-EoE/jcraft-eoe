package net.arna.jcraft.common.attack.core;

import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

/**
 * Anything that can use moves must implement this interface.
 * It provides basic functionality that moves need.
 * @param <A> The type of the class implementing this interface
 * @param <S> The type of the animation state enum
 */
public interface IAttacker<A extends IAttacker<? extends A, S>, S> {
    MoveContext getMoveContext();

    boolean hasUser();

    LivingEntity getUser();

    LivingEntity getUserOrThrow();

    int getMoveStun();

    void setMoveStun(int moveStun);

    // This cannot be called getWorld because it doesn't get remapped since it's in an interface.
    // StandEntity implements
    default World getEntityWorld() {
        return getBaseEntity().getWorld();
    }

    LivingEntity getBaseEntity();

    DamageSource getDamageSource();

    boolean canAttack();

    void cancelMove();

    AbstractMove<?, ? super A> getCurrentMove();

    void setCurrentMove(AbstractMove<?, ? super A> move);

    default void setMove(AbstractMove<?, ? super A> move, S state) {
        setCurrentMove(move);
        setState(state);
    }

    S getState();

    void setState(S state);

    void playSound(SoundEvent sound, float volume, float pitch);
}
