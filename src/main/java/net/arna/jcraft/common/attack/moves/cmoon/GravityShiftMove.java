package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Set;

public class GravityShiftMove extends AbstractMove<GravityShiftMove, CMoonEntity> {
    public GravityShiftMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CMoonEntity attacker, LivingEntity user, MoveContext ctx) {
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 350, 1));
        user.onLanding();
        attacker.setShiftTime(200);

        return Set.of();
    }

    @Override
    protected @NonNull GravityShiftMove getThis() {
        return this;
    }

    @Override
    public @NonNull GravityShiftMove copy() {
        return copyExtras(new GravityShiftMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
