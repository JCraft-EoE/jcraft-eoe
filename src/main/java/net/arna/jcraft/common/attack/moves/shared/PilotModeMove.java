package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class PilotModeMove<A extends StandEntity<? extends A, ?>> extends AbstractMove<PilotModeMove<A>, A> {
    public PilotModeMove(int cooldown) {
        super(cooldown, 0, 0, 0);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        attacker.togglePilotMode();
        return Set.of();
    }

    @Override
    protected @NonNull PilotModeMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull PilotModeMove<A> copy() {
        return copyExtras(new PilotModeMove<>(getCooldown()));
    }
}
