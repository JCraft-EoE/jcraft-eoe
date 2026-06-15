package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTimeStopMove;
import net.arna.jcraft.api.serverconfig.IntOption;
import net.arna.jcraft.api.stand.StandEntity;
import org.jetbrains.annotations.NotNull;

@Getter
public final class TimeStopMove<A extends StandEntity<? extends A, ?>> extends AbstractTimeStopMove<TimeStopMove<A>, A> {
    public TimeStopMove(final int cooldown, final int windup, final int duration, final Either<Integer, IntOption> timeStopDuration) {
        super(cooldown, windup, duration, 1f, timeStopDuration);
    }

    @Override
    public @NotNull MoveType<TimeStopMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull TimeStopMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TimeStopMove<A> copy() {
        return copyExtras(new TimeStopMove<>(getCooldown(), getWindup(), getDuration(), getTimeStopDuration()));
    }

    public static class Type extends AbstractTimeStopMove.Type<TimeStopMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<TimeStopMove<?>>, TimeStopMove<?>> buildCodec(RecordCodecBuilder.Instance<TimeStopMove<?>> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), timeStopDuration())
                    .apply(instance, applyExtras(TimeStopMove::new));
        }
    }
}
