package net.arna.jcraft.api.attack.moves;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.serverconfig.IntOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.function.Function;

@Setter
@Getter
public abstract class AbstractTimeStopMove<T extends AbstractTimeStopMove<T, A>, A extends StandEntity<? extends A, ?>> extends AbstractMove<T, A> {
    protected Either<Integer, IntOption> timeStopDuration;
    private static final MobEffectInstance tsBlind = new MobEffectInstance(MobEffects.BLINDNESS, 19, 0, true, false, false);

    protected AbstractTimeStopMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                                   final Either<Integer, IntOption> timeStopDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.timeStopDuration = timeStopDuration;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        int duration = timeStopDuration.map(Function.identity(), IntOption::getValue);
        attacker.setTsTime(duration);
        //attacker.setCurrentMove(null);
        if (user instanceof final Player player && !player.level().isClientSide()) {
            player.awardStat(JStatRegistry.TIME_STOPPED.get());
        }

        user.addEffect(new MobEffectInstance(tsBlind));

        JCraft.beginTimestop(user, attacker.position(), (ServerLevel) attacker.level(), duration);
        return Set.of();
    }

    protected abstract static class Type<M extends AbstractTimeStopMove<? extends M, ?>> extends AbstractMove.Type<M> {
        protected RecordCodecBuilder<M, Either<Integer, IntOption>> timeStopDuration() {
            return Codec.either(ExtraCodecs.POSITIVE_INT, JServerConfig.INT_OPTION_CODEC).fieldOf("time_stop_duration")
                    .forGetter(AbstractTimeStopMove::getTimeStopDuration);
        }
    }
}
