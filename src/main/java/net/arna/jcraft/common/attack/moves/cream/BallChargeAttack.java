package net.arna.jcraft.common.attack.moves.cream;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class BallChargeAttack extends AbstractMove<BallChargeAttack, CreamEntity> {
    public BallChargeAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CreamEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.playSound(JSoundRegistry.CREAM_CHARGE, 1, 1);
        attacker.setCharging(true);
        attacker.setChargeDir(user.getRotationVector().multiply(0.5));
        attacker.setVoidTime(15);

        return Set.of();
    }

    @Override
    protected @NonNull BallChargeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BallChargeAttack copy() {
        return copyExtras(new BallChargeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
