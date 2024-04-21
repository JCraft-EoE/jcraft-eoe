package net.arna.jcraft.common.attack.core;

import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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

    boolean initMove(MoveType type);

    boolean canHoldMove(@Nullable MoveInputType type);

    default void onUserMoveInput(AbstractMove<?, ? super A> currentMove, MoveInputType type, boolean pressed, boolean moveInitiated) {
        if ((moveInitiated && pressed && canHoldMove(type)) ){
            setHoldingType(type);
        }
        if (getHoldingType() == type) {
            setHolding(pressed);
        }
        if (currentMove != null) {
            currentMove.onUserMoveInput(getThis(), type, pressed, moveInitiated);
        }
    }

    boolean isHolding();

    void setHolding(boolean holding);

    MoveInputType getHoldingType();

    void setHoldingType(MoveInputType holdingType);

    boolean canAttack();

    void cancelMove();

    boolean isRemote();

    AbstractMove<?, ? super A> getCurrentMove();

    void setCurrentMove(AbstractMove<?, ? super A> move);

    default void setMove(AbstractMove<?, ? super A> move, S state) {
        setCurrentMove(move);
        setState(state);
    }

    S getState();

    void setState(S state);

    default void playAttackerSound(SoundEvent sound, float volume, float pitch)  {
        getBaseEntity().playSound(sound, volume, pitch);
    }

    A getThis();
}
