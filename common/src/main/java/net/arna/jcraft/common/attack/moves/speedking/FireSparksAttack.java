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
    /** Ticks between single-spark shots during hold */
    private final int shootInterval;
    /** Number of sparks fired in the initial burst */
    private final int initialBurst;
    /** Total spread angle of the initial burst in degrees */
    private final float burstSpread;
    /** Random spread half-range for held shots in degrees */
    private final float heldSpread;
    /** Base damage per spark */
    private final float sparkDamage;

    public FireSparksAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final int shootInterval, final int initialBurst, final float burstSpread,
                            final float heldSpread, final float sparkDamage) {
        super(cooldown, windup, duration, moveDistance);
        this.shootInterval = shootInterval;
        this.initialBurst = initialBurst;
        this.burstSpread = burstSpread;
        this.heldSpread = heldSpread;
        this.sparkDamage = sparkDamage;
        ranged = true;
        withHoldable(true);
    }

    @Override
    public @NonNull MoveType<FireSparksAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        // Initial burst — spread across burstSpread degrees
        for (int i = 0; i < initialBurst; i++) {
            float angleOffset = (initialBurst > 1)
                    ? (burstSpread / (initialBurst - 1)) * i - (burstSpread / 2f)
                    : 0f;
            shootSpark(attacker, user, angleOffset, 1.0f);
        }
        return Set.of();
    }

    @Override
    public void activeTick(SpeedKingEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);
        if (!attacker.hasUser()) return;

        // Stop as soon as the player releases the keybind
        if (!attacker.isHolding()) {
            attacker.setMoveStun(1);
            return;
        }

        // One spark every shootInterval ticks while held
        if (moveStun % shootInterval == 0) {
            LivingEntity user = attacker.getUserOrThrow();
            float spread = (attacker.level().random.nextFloat() - 0.5f) * heldSpread * 2f;
            shootSpark(attacker, user, spread, 1.0f);
        }
    }

    private void shootSpark(SpeedKingEntity attacker, LivingEntity user, float yawOffset, float speed) {
        final FireSparkProjectile spark = new FireSparkProjectile(attacker.level(), user);
        spark.setPos(getOffsetHeightPos(attacker));

        Vec3 dir = user.getLookAngle().yRot((float) Math.toRadians(yawOffset));
        spark.setDeltaMovement(dir.scale(speed));
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
                shootInterval, initialBurst, burstSpread, heldSpread, sparkDamage));
    }

    public static class Type extends AbstractMove.Type<FireSparksAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireSparksAttack>, FireSparksAttack> buildCodec(RecordCodecBuilder.Instance<FireSparksAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new FireSparksAttack(cd, wu, dur, md, 5, 5, 30f, 15f, 1.5f));
        }
    }
}
