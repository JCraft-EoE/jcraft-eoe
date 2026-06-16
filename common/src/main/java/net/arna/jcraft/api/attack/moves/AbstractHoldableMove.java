package net.arna.jcraft.api.attack.moves;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
public abstract class AbstractHoldableMove<T extends AbstractHoldableMove<T, A>, A extends IAttacker<A, ?>>
        extends AbstractMove<T, A> {
    protected boolean setMoveStun = false;
    private final int minimumCharge;
    private int chargeTime = 0;
    // Maximum charge is the end of the move

    protected AbstractHoldableMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                                   final int minimumCharge) {
        super(cooldown, windup, duration, moveDistance);
        this.minimumCharge = minimumCharge;

        withHoldable();
    }

    public T shouldSetMoveStun() {
        setMoveStun = true;
        return getThis();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        chargeTime = 0;
    }

    /**
     * Checks if the attacker is still using the given move.
     */
    private boolean sameMove(final A attacker, final MoveInputType type) {
        return type.getMoveClass() == getMoveClass();
    }

    @Override
    public void activeTick(final A attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        final boolean charged = moveStun <= getDuration() - minimumCharge;
        chargeTime++;
        if (moveStun == 1 || (charged && (!attacker.isHolding() || !sameMove(attacker, attacker.getHoldingType())))) {
            followUp(attacker);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public void onUserMoveInput(final A attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {
        if (!pressed && moveInitiated && sameMove(attacker, type)) {
            onRelease(attacker);
        }
    }

    protected void onRelease(final A attacker) {
        if (attacker.getMoveStun() <= getDuration() - minimumCharge) {
            followUp(attacker);
        }
    }

    private void followUp(final A attacker) {
        attacker.getMoveMap().initiateFollowup(attacker, getThis(), setMoveStun, chargeTime);
    }

    protected abstract static class Type<M extends AbstractHoldableMove<? extends M, ?>> extends AbstractMove.Type<M> {
        protected RecordCodecBuilder<M, Integer> minimumCharge() {
            return Codec.INT.fieldOf("minimum_charge").forGetter(AbstractHoldableMove::getMinimumCharge);
        }

        protected RecordCodecBuilder<M, Boolean> setMoveStun() {
            return Codec.BOOL.optionalFieldOf("set_move_stun", true).forGetter(AbstractHoldableMove::isSetMoveStun);
        }

        protected Products.P7<RecordCodecBuilder.Mu<M>, BaseMoveExtras, Integer, Integer, Integer, Float, Integer, Boolean>
        holdableDefault(final RecordCodecBuilder.Instance<M> instance) {
            return baseDefault(instance).and(instance.group(minimumCharge(), setMoveStun()));
        }

        protected App<RecordCodecBuilder.Mu<M>, M> holdableDefault(final RecordCodecBuilder.Instance<M> instance, Function5<
                Integer, Integer, Integer, Float, Integer, M> function) {
            return holdableDefault(instance).apply(instance, applyExtras((cooldown, windup, duration,
                                                                          moveDistance, minimumCharge, setMoveStun) -> {
                M move = function.apply(cooldown, windup, duration, moveDistance, minimumCharge);
                if (setMoveStun) {
                    move.shouldSetMoveStun();
                }
                return move;
            }));
        }
    }
}
