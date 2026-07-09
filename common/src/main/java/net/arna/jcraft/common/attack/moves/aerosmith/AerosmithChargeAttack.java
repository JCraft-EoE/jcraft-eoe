package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class AerosmithChargeAttack extends AbstractMultiHitAttack<AerosmithChargeAttack, AerosmithEntity> {
    Vec3 up, forward, peak, chargeStart, remoteChargeTarget;
    enum Segment { FIRST_RISE, CHARGE_START, CHARGE, RISE }
    Segment segment;
    boolean remoteCharge;

    public AerosmithChargeAttack(int cooldown, int duration, float damage, int stun, float hitboxSize, float knockback, float offset,
                                 @NonNull IntCollection hitMoments) {
        this(cooldown, duration, 0.0f, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    private AerosmithChargeAttack(int cooldown, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset,
                                    @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
        withLift(true);
    }

    @Override
    public void onInitiate(AerosmithEntity attacker) {
        super.onInitiate(attacker);

        remoteCharge = attacker.isRemote();

        final LivingEntity user = attacker.getUserOrThrow();

        if (!remoteCharge) {
            up = JUtils.getLocalUp(user);
            forward = user.getLookAngle();

            final Vec3 pos = user.position().add(up);
            final Vec3 toPeak = forward.add(up);

            segment = Segment.FIRST_RISE;
            peak = pos.add(toPeak);
            chargeStart = pos.add(forward);

            attacker.setRemote(true);
            attacker.setPos(pos.subtract(forward));
            attacker.setDeltaMovement(toPeak.normalize().scale(0.5));
            attacker.setFlyState(AerosmithEntity.FlyState.RETURN);
        } else {
            final Vec3 start = user.position();
            final HitResult goal = JUtils.raycastAll(user, start, start.add(user.getLookAngle().scale(64.0)), ClipContext.Fluid.NONE);
            chargeAt(attacker, goal.getLocation());
        }
    }

    public void chargeAt(final AerosmithEntity attacker, final Vec3 goal) {
        attacker.setFlyState(AerosmithEntity.FlyState.FLYBY);
        forward = attacker.getLookAngle();
        remoteChargeTarget = goal;
        segment = Segment.CHARGE;
    }

    @Override
    public void tick(AerosmithEntity attacker) {
        if (attacker.getCurrentMove() != this) return;

        if (remoteCharge) {
            attacker.setDeltaMovement(attacker.getDeltaMovement().scale(0.4).add(attacker.getLookAngle().scale(0.3)));
            attacker.allowXRotChange();
            attacker.lookAt(remoteChargeTarget, 10f, 10f);
            attacker.disallowXRotChange();
            return;
        }

        final int duration = getDuration();
        int time = duration - attacker.getMoveStun();

        switch (segment) {
            case FIRST_RISE -> {
                if (time > duration / 10) segment = Segment.CHARGE_START;
            }
            case CHARGE_START -> {
                final var pos = attacker.position();
                attacker.setDeltaMovement(chargeStart.subtract(pos).normalize().scale(0.4));
                if (time > duration * 2 / 10) segment = Segment.CHARGE;
            }
            case CHARGE -> {
                final var vel = attacker.getDeltaMovement();
                attacker.setDeltaMovement(vel.scale(0.6).add(forward.scale(0.2)));
                if (time > duration * 7 / 10) segment = Segment.RISE;
            }
            case RISE -> {
                final var vel = attacker.getDeltaMovement();
                attacker.setDeltaMovement(vel.scale(0.95).add(up.scale(0.04)));

                final double d = forward.x;
                final double f = forward.z;
                attacker.setXRot(89.0F);
                attacker.setYRot(Mth.wrapDegrees((float)(Mth.atan2(f, d) * Mth.RAD_TO_DEG) - 90.0F));
                attacker.setYHeadRot(attacker.getYRot());
            }
        }
    }

    @Override
    protected void performHook(AerosmithEntity attacker, Set<LivingEntity> targets, Set<AABB> boxes, DamageSource damageSource, Vec3 forwardPos, Vec3 rotationVector) {
        for (LivingEntity target : targets) {
            if (segment == Segment.RISE) {
                JUtils.addVelocity(target, up.x * 0.45, up.y * 0.45, up.z * 0.45);
            } else {
                // vacuum victims to front
                final Vec3 forceInFront = attacker.position().add(forward).subtract(target.position()).normalize().scale(0.65);
                JUtils.setVelocity(target, attacker.getDeltaMovement().scale(0.8).add(forceInFront));
            }
        }
    }

    @Override
    protected @NonNull AerosmithChargeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull AerosmithChargeAttack copy() {
        return copyExtras(new AerosmithChargeAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getOffset(), getHitMoments()));
    }

    @Override
    public @NonNull MoveType<AerosmithChargeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    public static class Type extends AbstractMultiHitAttack.Type<AerosmithChargeAttack> {
        public static final AerosmithChargeAttack.Type INSTANCE = new AerosmithChargeAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<AerosmithChargeAttack>, AerosmithChargeAttack> buildCodec(RecordCodecBuilder.Instance<AerosmithChargeAttack> instance) {
            return multiHitDefault(instance, AerosmithChargeAttack::new);
        }
    }
}
