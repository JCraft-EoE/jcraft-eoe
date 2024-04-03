package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class FlightMove extends AbstractMove<FlightMove, GEREntity> {
    public FlightMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.FLIGHT;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GEREntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.setFlightTime(20);
        return Set.of();
    }

    public void tickFlight(GEREntity attacker) {
        if (!attacker.hasUser()) return;
        LivingEntity user = attacker.getUserOrThrow();
        // Must be run on client and server because of fun mod compatibility
        int flightTime = attacker.getFlightTime();
        flightTime -= 1;
        attacker.setFlightTime(flightTime);
        if (user instanceof PlayerEntity playerEntity) {
            if (!playerEntity.isCreative() && !playerEntity.isSpectator())
                playerEntity.getAbilities().flying = (flightTime > 1);
        } else if (flightTime > 1) {
            double y = user.getY();
            Vec3d vel = new Vec3d(user.getVelocity().x, 0.0, user.getVelocity().z);
            // Targetting priority
            LivingEntity targetEntity = user.getDamageTracker().getBiggestFall();
            if (targetEntity == null && user instanceof MobEntity mob)
                targetEntity = mob.getTarget();
            if (targetEntity == null)
                targetEntity = user.getAttacker();
            // If target wasn't found, search in a radius
            Vec3d target = targetEntity != null ? targetEntity.getEyePos() :
                    attacker.getPos().add(Math.sin(attacker.age * 0.2) * 3, 0, Math.cos(attacker.age * 0.2) * 3);

            double dY = MathHelper.clamp(target.getY() - y, -1, 1);
            y += dY;

            vel = vel.add(target.subtract(user.getPos()).normalize()).multiply(0.4);

            user.setVelocity(vel);
            user.setPos(user.getX(), y, user.getZ());

            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 1, true, false));
        }
    }

    @Override
    protected @NonNull FlightMove getThis() {
        return this;
    }

    @Override
    public @NonNull FlightMove copy() {
        return copyExtras(new FlightMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
