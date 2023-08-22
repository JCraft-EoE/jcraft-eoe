package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public abstract class AbstractTimestopAttack<T extends AbstractTimestopAttack<T, A>, A extends IAttacker<?, ?>> extends AbstractMove<T, A> {
    @Getter
    @Setter
    protected int timestopDuration;
    private static final StatusEffectInstance tsBlind = new StatusEffectInstance(StatusEffects.BLINDNESS, 19, 0, true, false, false);

    protected AbstractTimestopAttack(int cooldown, int windup, int duration, float moveDistance, int tsDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.timestopDuration = tsDuration;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        if (attacker instanceof StandEntity<?,?> stand) {
            stand.setTsTime(timestopDuration);
            stand.setCurrentMove(null);

            user.addStatusEffect(tsBlind);

            JCraft.beginTimestop(user, stand.getPos(), (ServerWorld) stand.getWorld(), timestopDuration);
        }

        return Set.of();
    }
}
