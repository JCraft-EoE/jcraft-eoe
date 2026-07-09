package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;

public final class TossMove<A extends IAttacker<? extends A, ?>> extends AbstractTossMove<TossMove<A>, A> {

    public TossMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public TossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public TossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier, float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<TossMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull TossMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TossMove<A> copy() {
        return copyExtras(new TossMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<TossMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TossMove<?>>, TossMove<?>> buildCodec(RecordCodecBuilder.Instance<TossMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, TossMove::new);
        }
    }
}
