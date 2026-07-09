package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public final class FlightMove extends AbstractMove<FlightMove, GEREntity> {
    private final int flightTime;

    public FlightMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int flightTime) {
        super(cooldown, windup, duration, moveDistance);
        this.flightTime = flightTime;
        mobilityType = MobilityType.FLIGHT;
    }

    @Override
    public @NotNull MoveType<FlightMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final GEREntity attacker) {
        tickFlight(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final GEREntity attacker, final LivingEntity user) {
        attacker.setFlightTime(flightTime);
        return Set.of();
    }

    public void tickFlight(final GEREntity attacker) {
        if (!attacker.hasUser()) {
            return;
        }
        final LivingEntity user = attacker.getUserOrThrow();
        // Must be run on client and server because of fun mod compatibility
        int flightTime = attacker.getFlightTime();
        flightTime -= 1;
        attacker.setFlightTime(flightTime);
        if (!(user instanceof Player) && flightTime > 1) {
            double y = user.getY();
            Vec3 vel = new Vec3(user.getDeltaMovement().x, 0.0, user.getDeltaMovement().z);
            // Targetting priority
            LivingEntity targetEntity = null;
            final CombatEntry mostDamagingOpponent = user.getCombatTracker().getMostSignificantFall();
            if (mostDamagingOpponent != null && mostDamagingOpponent.source().getEntity() instanceof LivingEntity livingTarget) {
                targetEntity = livingTarget;
            }
            if (targetEntity == null && user instanceof Mob mob) {
                targetEntity = mob.getTarget();
            }
            if (targetEntity == null) {
                targetEntity = user.getLastHurtByMob();
            }
            // If target wasn't found, search in a radius
            final Vec3 target = targetEntity != null ? targetEntity.getEyePosition() :
                    attacker.position().add(Math.sin(attacker.tickCount * 0.2) * 3, 0, Math.cos(attacker.tickCount * 0.2) * 3);

            double dY = Mth.clamp(target.y() - y, -1, 1);
            y += dY;

            vel = vel.add(target.subtract(user.position()).normalize()).scale(0.4);

            user.setDeltaMovement(vel);
            user.setPosRaw(user.getX(), y, user.getZ());

            user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 1, true, false));
        }
    }

    @Override
    protected @NonNull FlightMove getThis() {
        return this;
    }

    @Override
    public @NonNull FlightMove copy() {
        return copyExtras(new FlightMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getFlightTime()));
    }

    public static class Type extends AbstractMove.Type<FlightMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<FlightMove>, FlightMove> buildCodec(RecordCodecBuilder.Instance<FlightMove> instance) {
            return baseDefault(instance).and(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("flight_time").forGetter(FlightMove::getFlightTime))
                    .apply(instance, applyExtras(FlightMove::new));
        }
    }
}
