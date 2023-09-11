package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;
import java.util.function.IntSupplier;

@Getter
public abstract class AbstractTimeStopMove<T extends AbstractTimeStopMove<T, A>, A extends StandEntity<? extends A, ?>> extends AbstractMove<T, A> {
    @Setter
    protected IntSupplier timeStopDuration;
    private static final StatusEffectInstance tsBlind = new StatusEffectInstance(StatusEffects.BLINDNESS, 19, 0, true, false, false);

    protected AbstractTimeStopMove(int cooldown, int windup, int duration, float moveDistance, IntSupplier timeStopDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.timeStopDuration = timeStopDuration;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        attacker.setTsTime(timeStopDuration.getAsInt());
        attacker.setCurrentMove(null);

        user.addStatusEffect(new StatusEffectInstance(tsBlind));

        JCraft.beginTimestop(user, attacker.getPos(), (ServerWorld) attacker.getWorld(), timeStopDuration.getAsInt());
        return Set.of();
    }
}
