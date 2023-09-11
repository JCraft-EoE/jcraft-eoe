package net.arna.jcraft.common.attack.moves.cream;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

@Getter
public class BallModeMove extends AbstractMove<BallModeMove, CreamEntity> {
    private final boolean enter;

    public BallModeMove(int cooldown, int windup, int duration, float moveDistance, boolean enter) {
        super(cooldown, windup, duration, moveDistance);
        this.enter = enter;
        if (enter) mobilityType = MobilityType.FLIGHT;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CreamEntity attacker, LivingEntity user, MoveContext ctx) {
        if (enter) attacker.beginHalfBall();
        else attacker.endHalfBall();
        return Set.of();
    }

    @Override
    protected @NonNull BallModeMove getThis() {
        return this;
    }

    @Override
    public @NonNull BallModeMove copy() {
        return copyExtras(new BallModeMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), isEnter()));
    }
}
