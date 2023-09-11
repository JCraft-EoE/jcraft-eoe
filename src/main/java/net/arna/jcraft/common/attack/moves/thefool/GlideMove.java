package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class GlideMove extends AbstractMove<GlideMove, TheFoolEntity> {
    public GlideMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.FLIGHT;
    }

    @Override
    public void tick(TheFoolEntity attacker) {
        super.tick(attacker);

        LivingEntity user = attacker.getUser();
        if (user == null) return;

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 4, 4, true, false));
        double yVel = attacker.getRemoteJumpInput() ? 0.07 : 0;
        Vec3d rotVec = user.getRotationVector().multiply(0.04);
        user.addVelocity(rotVec.x, yVel, rotVec.z);
        user.velocityModified = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.setSand(false); // Ends transformation state
        return Set.of();
    }

    @Override
    protected @NonNull GlideMove getThis() {
        return this;
    }

    @Override
    public @NonNull GlideMove copy() {
        return copyExtras(new GlideMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
