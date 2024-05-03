package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

@Getter
public class JumpMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<JumpMove<A>, A> {
    private final float strength;

    public JumpMove(int cooldown, int windup, int duration, float moveDistance, float strength) {
        super(cooldown, windup, duration, moveDistance);
        this.strength = strength;
        mobilityType = MobilityType.DASH;
    }

    @Override
    public boolean canBeInitiated(A attacker) {
        return super.canBeInitiated(attacker) && attacker.getUser().isOnGround();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        if (!user.isOnGround()) return Set.of();
        Vec3d upVel = Vec3d.of(GravityChangerAPI.getGravityDirection(user).getVector()).multiply(-0.5);
        Vec3d jumpVel = Vec3d.fromPolar(user.getPitch(), user.getYaw()).multiply(strength).add(upVel);
        JUtils.setVelocity(user, jumpVel.x, jumpVel.y, jumpVel.z);

        return Set.of();
    }

    @Override
    protected @NonNull JumpMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull JumpMove<A> copy() {
        return copyExtras(new JumpMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), strength));
    }
}
