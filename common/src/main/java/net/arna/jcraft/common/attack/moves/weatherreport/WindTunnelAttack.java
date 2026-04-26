package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public final class WindTunnelAttack extends AbstractMove<WindTunnelAttack, WeatherReportEntity> {

    private final double tunnelLength;
    private final double tunnelRadius;

    private Vec3 tunnelOrigin;
    private Vec3 tunnelDir;

    public WindTunnelAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final double tunnelLength, final double tunnelRadius) {
        super(cooldown, windup, duration, moveDistance);
        this.tunnelLength = tunnelLength;
        this.tunnelRadius = tunnelRadius;
    }

    @Override
    public void onInitiate(final WeatherReportEntity attacker) {
        super.onInitiate(attacker);
        final LivingEntity user = attacker.getUserOrThrow();
        tunnelOrigin = user.getEyePosition();
        tunnelDir = user.getLookAngle().normalize();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public void activeTick(final WeatherReportEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (attacker.level().isClientSide || tunnelOrigin == null || tunnelDir == null) return;

        final double windSpeed = attacker.getWindTunnelSpeed();
        final LivingEntity user = attacker.getUserOrThrow();

        final AABB broadBox = AABB.ofSize(
                tunnelOrigin.add(tunnelDir.scale(tunnelLength / 2)),
                tunnelLength, tunnelLength, tunnelLength);

        final List<LivingEntity> candidates = attacker.level().getEntitiesOfClass(LivingEntity.class, broadBox,
                EntitySelector.ENTITY_STILL_ALIVE);

        for (final LivingEntity entity : candidates) {
            if (!isInsideTunnel(entity.position())) continue;
            entity.setDeltaMovement(entity.getDeltaMovement().add(tunnelDir.scale(windSpeed)));
            entity.hurtMarked = true;
        }

        if (isInsideTunnel(user.position())) {
            user.setDeltaMovement(user.getDeltaMovement().add(tunnelDir.scale(windSpeed)));
            user.hurtMarked = true;
        }

        if (attacker.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3; i++) {
                final double t = attacker.getRandom().nextDouble() * tunnelLength;
                final Vec3 p = tunnelOrigin.add(tunnelDir.scale(t))
                        .add(randomLateral(attacker) * (tunnelRadius - 0.2),
                             randomLateral(attacker) * (tunnelRadius - 0.2),
                             randomLateral(attacker) * (tunnelRadius - 0.2));
                serverLevel.sendParticles(ParticleTypes.CLOUD, p.x, p.y, p.z,
                        1, tunnelDir.x * 0.2, tunnelDir.y * 0.2, tunnelDir.z * 0.2, 0.02);
            }
        }
    }

    private boolean isInsideTunnel(final Vec3 pos) {
        if (tunnelOrigin == null || tunnelDir == null) return false;
        final Vec3 delta = pos.subtract(tunnelOrigin);
        final double axisProj = delta.dot(tunnelDir);
        if (axisProj < 0 || axisProj > tunnelLength) return false;
        final Vec3 lateral = delta.subtract(tunnelDir.scale(axisProj));
        return lateral.lengthSqr() <= tunnelRadius * tunnelRadius;
    }

    private double randomLateral(final WeatherReportEntity attacker) {
        return (attacker.getRandom().nextDouble() * 2 - 1) * tunnelRadius;
    }

    @Override
    public @NonNull MoveType<WindTunnelAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull WindTunnelAttack getThis() {
        return this;
    }

    @Override
    public @NonNull WindTunnelAttack copy() {
        return copyExtras(new WindTunnelAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), tunnelLength, tunnelRadius));
    }

    public static class Type extends AbstractMove.Type<WindTunnelAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<WindTunnelAttack>, WindTunnelAttack> buildCodec(RecordCodecBuilder.Instance<WindTunnelAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) -> new WindTunnelAttack(cd, wu, dur, md, 15.0, 2.0));
        }
    }
}
