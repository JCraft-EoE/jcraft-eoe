package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;

public final class SpinningNailChargeMove<A extends IAttacker<A, ?>> extends AbstractHoldableMove<SpinningNailChargeMove<A>, A> {
    public SpinningNailChargeMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int minimumCharge) {
        super(cooldown, windup, duration, moveDistance, minimumCharge);
    }

    @Override
    public @NonNull MoveType<SpinningNailChargeMove<A>> getMoveType() {
        return (MoveType<SpinningNailChargeMove<A>>) (MoveType<?>) Type.INSTANCE;
    }

    @Override
    protected @NonNull SpinningNailChargeMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SpinningNailChargeMove<A> copy() {
        SpinningNailChargeMove<A> copy = new SpinningNailChargeMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getMinimumCharge());
        if (setMoveStun) {
            copy.shouldSetMoveStun();
        }
        return copyExtras(copy);
    }

    public static class Type extends AbstractHoldableMove.Type<SpinningNailChargeMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SpinningNailChargeMove<?>>, SpinningNailChargeMove<?>> buildCodec(RecordCodecBuilder.Instance<SpinningNailChargeMove<?>> instance) {
            return holdableDefault(instance, SpinningNailChargeMove::new);
        }
    }
}