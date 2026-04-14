package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.FireSparkProjectile;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class FireSparksAttack extends AbstractMove<FireSparksAttack, SpeedKingEntity> {
    private static final float HELD_SPREAD = 15f;

    private final int shootInterval;
    private final float sparkDamage;

    public FireSparksAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final int shootInterval, final float sparkDamage) {
        super(cooldown, windup, duration, moveDistance);
        this.shootInterval = shootInterval;
        this.sparkDamage = sparkDamage;
        ranged = true;
        withHoldable(true);
        manualCooldown = true;
    }

    @Override
    public @NonNull MoveType<FireSparksAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        shootSpark(attacker, user, 0f, 1.0f);
        return Set.of();
    }

    @Override
    public void activeTick(SpeedKingEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);
        if (!attacker.hasUser()) return;

        if (moveStun == 0) {
            JComponentPlatformUtils.getCooldowns(attacker.getUserOrThrow())
                    .setCooldown(getMoveClass().getDefaultCooldownType(), getCooldown());
            return;
        }

        if (!attacker.isHolding()) {
            JComponentPlatformUtils.getCooldowns(attacker.getUserOrThrow())
                    .setCooldown(getMoveClass().getDefaultCooldownType(), getCooldown());
            attacker.setMoveStun(1);
            return;
        }

        if (moveStun % shootInterval == 0) {
            LivingEntity user = attacker.getUserOrThrow();
            float spread = (attacker.level().random.nextFloat() - 0.5f) * HELD_SPREAD * 2f;
            shootSpark(attacker, user, spread, 1.0f);
        }
    }

    private void shootSpark(SpeedKingEntity attacker, LivingEntity user, float yawOffset, float speed) {
        final FireSparkProjectile spark = new FireSparkProjectile(attacker.level(), user);
        spark.setPos(getOffsetHeightPos(attacker));
        spark.setDeltaMovement(user.getLookAngle().yRot((float) Math.toRadians(yawOffset)).scale(speed));
        spark.hurtMarked = true;
        spark.setBaseDamage(sparkDamage);
        attacker.level().addFreshEntity(spark);
    }

    @Override
    protected @NonNull FireSparksAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FireSparksAttack copy() {
        return copyExtras(new FireSparksAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                shootInterval, sparkDamage));
    }

    public static class Type extends AbstractMove.Type<FireSparksAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireSparksAttack>, FireSparksAttack> buildCodec(RecordCodecBuilder.Instance<FireSparksAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new FireSparksAttack(cd, wu, dur, md, 5, 1.5f));
        }
    }
}
