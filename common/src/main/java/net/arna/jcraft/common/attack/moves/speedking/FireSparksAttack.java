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

import java.util.Set;

public final class FireSparksAttack extends AbstractMove<FireSparksAttack, SpeedKingEntity> {

    public FireSparksAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
        withHoldable(true); // This is a holdable attack
    }

    @Override
    public @NonNull MoveType<FireSparksAttack> getMoveType() {
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

            spark.setDeltaMovement(spreadDirection.scale(1.0f));
            spark.hurtMarked = true;

            // Configure for Fire Sparks Heavy - no stun, chip damage, boiling
            spark.setBaseDamage(1.5f); // Minor chip damage - this will trigger the boiling-only mode

            attacker.level().addFreshEntity(spark);
        }

        return Set.of();
    }

    @Override
    public void activeTick(SpeedKingEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        // Continue shooting sparks while held (every 5 ticks)
        if (moveStun % 5 == 0 && attacker.hasUser()) {
            final FireSparkProjectile spark = new FireSparkProjectile(attacker.level(), attacker.getUserOrThrow());

            Vec3 startPos = getOffsetHeightPos(attacker);
            spark.setPos(startPos);

            Vec3 direction = attacker.getUserOrThrow().getLookAngle();
            // Add slight random spread
            Vec3 spreadDirection = direction.yRot((float) Math.toRadians((attacker.level().random.nextFloat() - 0.5f) * 20f));

            spark.setDeltaMovement(spreadDirection.scale(1.0f));
            spark.hurtMarked = true;

            spark.setBaseDamage(1.5f); // Low damage triggers boiling-only behavior in the projectile

            attacker.level().addFreshEntity(spark);
        }
    }

    @Override
    protected @NonNull FireSparksAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FireSparksAttack copy() {
        return copyExtras(new FireSparksAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<FireSparksAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireSparksAttack>, FireSparksAttack> buildCodec(RecordCodecBuilder.Instance<FireSparksAttack> instance) {
            return baseDefault(instance, FireSparksAttack::new);
        }
    }
}