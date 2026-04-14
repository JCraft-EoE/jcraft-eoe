package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.FireSparkProjectile;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class HeatTrapAttack extends AbstractMove<HeatTrapAttack, SpeedKingEntity> {
    private static final int PROJECTILE_COUNT = 5;
    private static final float SPREAD_DEGREES = 40f;
    private static final float PROJECTILE_SPEED = 1.2f;

    public HeatTrapAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<HeatTrapAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        for (int i = 0; i < PROJECTILE_COUNT; i++) {
            float yawOffset = (SPREAD_DEGREES / (PROJECTILE_COUNT - 1)) * i - (SPREAD_DEGREES / 2f);

            FireSparkProjectile spark = new FireSparkProjectile(attacker.level(), user);
            spark.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            spark.setDeltaMovement(user.getLookAngle().yRot((float) Math.toRadians(yawOffset)).scale(PROJECTILE_SPEED));
            spark.hurtMarked = true;
            spark.setHeatTrapMode(true);

            attacker.level().addFreshEntity(spark);
        }

        return Set.of();
    }

    @Override
    protected @NonNull HeatTrapAttack getThis() { return this; }

    @Override
    public @NonNull HeatTrapAttack copy() {
        return copyExtras(new HeatTrapAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<HeatTrapAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HeatTrapAttack>, HeatTrapAttack> buildCodec(RecordCodecBuilder.Instance<HeatTrapAttack> instance) {
            return baseDefault(instance, HeatTrapAttack::new);
        }
    }
}
