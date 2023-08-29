package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
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
        return attacker.getBaseEntity().isOnGround() && super.canBeInitiated(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        if (!user.isOnGround()) return Set.of();

        Vec3d jumpVel = getRotVec(attacker).multiply(strength).add(0, 0.5, 0);

        user.addVelocity(jumpVel.x, jumpVel.y, jumpVel.z);
        user.velocityModified = true;

        if (user instanceof ServerPlayerEntity player)
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));

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
