package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSpinMove;

public final class SpinMove<A extends IAttacker<? extends A, ?>> extends AbstractSpinMove<SpinMove<A>, A> {

    public SpinMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public SpinMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public SpinMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier, float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<SpinMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull SpinMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SpinMove<A> copy() {
        return copyExtras(new SpinMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractSpinMove.Type<SpinMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SpinMove<?>>, SpinMove<?>> buildCodec(RecordCodecBuilder.Instance<SpinMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, SpinMove::new);
        }
    }
}
