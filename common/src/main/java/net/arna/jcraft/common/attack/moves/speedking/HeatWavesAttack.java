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

    public HeatWavesAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<HeatWavesAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        final int sparkCount = 5;
        final float spreadAngle = 30f;

        for (int i = 0; i < sparkCount; i++) {
            float angleOffset = (spreadAngle / (sparkCount - 1)) * i - (spreadAngle / 2);

            final FireSparkProjectile spark = new FireSparkProjectile(attacker.level(), user);

            Vec3 startPos = getOffsetHeightPos(attacker);
            spark.setPos(startPos);

            Vec3 direction = user.getLookAngle();
            Vec3 spreadDirection = direction.yRot((float) Math.toRadians(angleOffset));

            spark.setDeltaMovement(spreadDirection.scale(1.2f));
            spark.hurtMarked = true;

            // Configure for Heat Waves - bouncing mode with heat wave creation
            spark.setBouncingMode(true);
            spark.setBaseDamage(2.0f);
            spark.setBounceCount(0);
            spark.setMaxBounces(3);
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
        return copyExtras(new HeatWavesAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<HeatWavesAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HeatWavesAttack>, HeatWavesAttack> buildCodec(RecordCodecBuilder.Instance<HeatWavesAttack> instance) {
            return baseDefault(instance, HeatWavesAttack::new);
        }
    }
}