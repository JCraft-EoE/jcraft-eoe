package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.FireSparkProjectile;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HeatWavesAttack extends AbstractMove<HeatWavesAttack, SpeedKingEntity> {
    private static final List<FireSparkProjectile> ACTIVE_HEAT_WAVE_PROJECTILES = new ArrayList<>();

    /** Number of sparks fired per volley */
    private final int sparkCount;
    /** Total spread angle of the volley in degrees */
    private final float spreadAngle;
    /** Projectile speed */
    private final float sparkSpeed;
    /** Base damage per spark */
    private final float sparkDamage;
    /** Maximum number of bounces per spark */
    private final int maxBounces;

    public HeatWavesAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final int sparkCount, final float spreadAngle, final float sparkSpeed,
                           final float sparkDamage, final int maxBounces) {
        super(cooldown, windup, duration, moveDistance);
        this.sparkCount = sparkCount;
        this.spreadAngle = spreadAngle;
        this.sparkSpeed = sparkSpeed;
        this.sparkDamage = sparkDamage;
        this.maxBounces = maxBounces;
        ranged = true;
    }

    @Override
    public @NonNull MoveType<HeatWavesAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        for (int i = 0; i < sparkCount; i++) {
            float angleOffset = (sparkCount > 1)
                    ? (spreadAngle / (sparkCount - 1)) * i - (spreadAngle / 2)
                    : 0f;

            final FireSparkProjectile spark = new FireSparkProjectile(attacker.level(), user);

            Vec3 startPos = getOffsetHeightPos(attacker);
            spark.setPos(startPos);

            Vec3 direction = user.getLookAngle();
            Vec3 spreadDirection = direction.yRot((float) Math.toRadians(angleOffset));

            spark.setDeltaMovement(spreadDirection.scale(sparkSpeed));
            spark.hurtMarked = true;
            spark.setBouncingMode(true);
            spark.setBaseDamage(sparkDamage);
            spark.setBounceCount(0);
            spark.setMaxBounces(maxBounces);
            spark.setHeatWaveMode(true);

            attacker.level().addFreshEntity(spark);
            ACTIVE_HEAT_WAVE_PROJECTILES.add(spark);
        }

        return Set.of();
    }

    public static void cleanupProjectile(FireSparkProjectile projectile) {
        ACTIVE_HEAT_WAVE_PROJECTILES.remove(projectile);
    }

    @Override
    protected @NonNull HeatWavesAttack getThis() {
        return this;
    }

    @Override
    public @NonNull HeatWavesAttack copy() {
        return copyExtras(new HeatWavesAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                sparkCount, spreadAngle, sparkSpeed, sparkDamage, maxBounces));
    }

    public static class Type extends AbstractMove.Type<HeatWavesAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HeatWavesAttack>, HeatWavesAttack> buildCodec(RecordCodecBuilder.Instance<HeatWavesAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new HeatWavesAttack(cd, wu, dur, md, 5, 30f, 1.2f, 2.0f, 3));
        }
    }
}
