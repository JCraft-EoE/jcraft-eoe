package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public final class WindMovementDetectionMove extends AbstractMove<WindMovementDetectionMove, WeatherReportEntity> {

    private final float detectionRange;

    public WindMovementDetectionMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                                     final float detectionRange) {
        super(cooldown, windup, duration, moveDistance);
        this.detectionRange = detectionRange;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public void activeTick(final WeatherReportEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (!attacker.level().isClientSide) return;

        final LivingEntity user = attacker.getUser();
        if (user == null) return;

        final AABB searchBox = AABB.ofSize(user.position(), detectionRange * 2, detectionRange, detectionRange * 2);
        final List<LivingEntity> nearby = attacker.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                EntitySelector.ENTITY_STILL_ALIVE.and(e -> e != user && e != attacker.getBaseEntity()));

        for (final LivingEntity entity : nearby) {
            final Vec3 velocity = entity.getDeltaMovement();
            final boolean isSneaking = entity.isDiscrete();
            if (velocity.lengthSqr() < 1e-4 && !isSneaking) continue;

            final Vec3 dir = velocity.lengthSqr() > 1e-4 ? velocity.normalize() : Vec3.ZERO;
            final Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

            for (int i = 0; i < 8; i++) {
                final double t = i * 0.35;
                final Vec3 p = entityPos.add(dir.scale(-t));
                attacker.level().addParticle(ParticleTypes.CLOUD,
                        p.x + attacker.getRandom().nextGaussian() * 0.07,
                        p.y + attacker.getRandom().nextGaussian() * 0.07,
                        p.z + attacker.getRandom().nextGaussian() * 0.07,
                        dir.x * 0.04, 0, dir.z * 0.04);
            }
            if (isSneaking) {
                for (int i = 0; i < 6; i++) {
                    final double a = i * (Math.PI / 3);
                    attacker.level().addParticle(ParticleTypes.POOF,
                            entityPos.x + Math.cos(a) * 0.5,
                            entityPos.y,
                            entityPos.z + Math.sin(a) * 0.5,
                            0, 0.02, 0);
                }
            } else {
                attacker.level().addParticle(ParticleTypes.CRIT,
                        entityPos.x, entityPos.y, entityPos.z,
                        dir.x * 0.1, 0.05, dir.z * 0.1);
            }
        }
    }

    @Override
    public @NonNull MoveType<WindMovementDetectionMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull WindMovementDetectionMove getThis() {
        return this;
    }

    @Override
    public @NonNull WindMovementDetectionMove copy() {
        return copyExtras(new WindMovementDetectionMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), detectionRange));
    }

    public static class Type extends AbstractMove.Type<WindMovementDetectionMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<WindMovementDetectionMove>, WindMovementDetectionMove> buildCodec(RecordCodecBuilder.Instance<WindMovementDetectionMove> instance) {
            return baseDefault(instance, (cd, wu, dur, md) -> new WindMovementDetectionMove(cd, wu, dur, md, 20f));
        }
    }
}
