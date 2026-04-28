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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class AerosmithChargeAttack extends AbstractMultiHitAttack<AerosmithChargeAttack, AerosmithEntity> {
    Vec3 up, forward, peak, chargeStart;
    enum Segment { FIRST_RISE, CHARGE_START, CHARGE, RISE }
    Segment segment;

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

        final boolean remoteVersion = attacker.isRemote();

        if (!remoteVersion) {
            final LivingEntity user = attacker.getUserOrThrow();

            up = JUtils.getLocalUp(user);
            forward = user.getLookAngle();

            final Vec3 pos = user.position().add(up);
            final Vec3 toPeak = forward.add(up.scale(3));

            segment = Segment.FIRST_RISE;
            peak = pos.add(toPeak);
            chargeStart = pos.add(forward);

            attacker.setRemote(true);
            attacker.setPos(pos.subtract(forward));
            attacker.setDeltaMovement(toPeak.normalize().scale(0.5));
            attacker.setFlyState(AerosmithEntity.FlyState.RETURN);
        } else {
            // TODO: REMOTE AERO CHARGE
        }
    }

    @Override
    public void tick(AerosmithEntity attacker) {
        if (attacker.getCurrentMove() != this) return;

        final int duration = getDuration();
        int time = duration - attacker.getMoveStun();

        if (segment == Segment.FIRST_RISE) {
            if (time > duration / 10) segment = Segment.CHARGE_START;
        }

        else if (segment == Segment.CHARGE_START) {
            final var pos = attacker.position();
            attacker.setDeltaMovement(chargeStart.subtract(pos).normalize().scale(0.4));
            if (time > duration * 2 / 10) segment = Segment.CHARGE;
        }

        else if (segment == Segment.CHARGE) {
            final var vel = attacker.getDeltaMovement();
            attacker.setDeltaMovement(vel.scale(0.6).add(forward.scale(0.2)));
            if (time > duration * 6 / 8) segment = Segment.RISE;
        }

        else if (segment == Segment.RISE) {
            final var vel = attacker.getDeltaMovement();
            attacker.setDeltaMovement(vel.scale(0.95).add(up.scale(0.04)));

            final double d = forward.x;
            final double f = forward.z;
            attacker.setXRot(89.0F);
            attacker.setYRot(Mth.wrapDegrees((float)(Mth.atan2(f, d) * Mth.RAD_TO_DEG) - 90.0F));
            attacker.setYHeadRot(attacker.getYRot());
        }
    }

    @Override
    protected void performHook(AerosmithEntity attacker, Set<LivingEntity> targets, Set<AABB> boxes, DamageSource damageSource, Vec3 forwardPos, Vec3 rotationVector) {
        for (LivingEntity target : targets) {
            if (segment == Segment.RISE) {
                JUtils.addVelocity(target, up.x * 0.45, up.y * 0.45, up.z * 0.45);
            } else {
                // vacuum victims to front
                final Vec3 forceInFront = attacker.position().add(forward).subtract(target.position()).normalize().scale(0.5);
                JUtils.setVelocity(target, attacker.getDeltaMovement().scale(0.7).add(forceInFront));
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
