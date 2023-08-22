package net.arna.jcraft.common.attack.moves.whitesnake;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class PilotModeMove extends AbstractMove<PilotModeMove, WhiteSnakeEntity> {
    public PilotModeMove(int cooldown) {
        super(cooldown, 0, 0, 0);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(WhiteSnakeEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.togglePilotMode();
        return Set.of();
    }

    @Override
    protected @NonNull PilotModeMove getThis() {
        return this;
    }

    @Override
    public @NonNull PilotModeMove copy() {
        return null;
    }
}
