package net.arna.jcraft.common.attack.moves.ranger;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.network.c2s.PlayerInputPacket;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.common.util.InputStateManager;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class RangerSlideMove extends AbstractSimpleAttack<RangerSlideMove, RangerSpec> {
    private static final double SLIDE_SPEED = 0.75;
    private static final double END_HITBOX_SIZE = 0.75;

    private Vec3 lockedDirection; // null while steering with the camera

    public RangerSlideMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        mobilityType = MobilityType.DASH;
        withHoldable();
    }

    @Override
    public void onInitiate(final RangerSpec attacker) {
        super.onInitiate(attacker);
        lockedDirection = null;

        // A/S/D locks the slide to the direction faced at the start; forward or no input stays steerable
        if (attacker.getUser() instanceof ServerPlayer player) {
            final InputStateManager input = PlayerInputPacket.getInputStateManager(player);
            if (input.calcSide() != 0 || input.calcForward() == -1) {
                lockedDirection = Vec3.directionFromRotation(0, player.getYRot()).scale(SLIDE_SPEED);
            }
        }
    }

    @Override
    public void activeTick(final RangerSpec attacker, final int moveStun) {
        if (!attacker.isHolding() || moveStun <= 0) {
            attacker.cancelMove();
            return;
        }

        final LivingEntity user = attacker.getUser();
        if (user == null) {
            return;
        }

        // user.setPose(Pose.SWIMMING);

        if (user.onGround()) {
            final Vec3 delta = user.getDeltaMovement();
            final Vec3 next = delta.add(slideDirection(user)).scale(0.5);
            user.setDeltaMovement(next.x, delta.y, next.z);
            user.hurtMarked = true;
        }

        super.activeTick(attacker, moveStun);
    }

    // Slides forward steered by the camera, unless locked to an input direction
    private Vec3 slideDirection(final LivingEntity user) {
        return lockedDirection != null ? lockedDirection
                : Vec3.directionFromRotation(0, user.getYRot()).scale(SLIDE_SPEED);
    }

    @Override
    public boolean shouldPerform(final RangerSpec attacker, final int moveStun) {
        return attacker.hasUser() && moveStun % 2 == 0;
    }

    @Override
    protected Set<AABB> calculateBoxes(final RangerSpec attacker, final LivingEntity user, final Vec3 rotVec,
                                       final Vec3 upVec, final Vec3 hPos, final Vec3 fPos) {
        // Shrinks from the configured size down to END_HITBOX_SIZE over the slide's duration
        final double progress = 1.0 - (double) attacker.getMoveStun() / getDuration();
        final double size = Mth.lerp(progress, getHitboxSize(), END_HITBOX_SIZE);
        return Set.of(createBox(user.getBoundingBox().getCenter(), size).expandTowards(slideDirection(user)));
    }

    @Override
    protected void performHook(final RangerSpec attacker, final Set<LivingEntity> targets, final Set<AABB> boxes,
                               final DamageSource damageSource, final Vec3 forwardPos, final Vec3 rotationVector) {
        final LivingEntity user = attacker.getUserOrThrow();
        final Vec3 forward = Vec3.directionFromRotation(0, user.getYRot());
        for (final LivingEntity target : targets) {
            // vacuum victims to front, carrying them along with the slide
            final Vec3 forceInFront = user.position().add(forward).subtract(target.position()).normalize().scale(0.65);
            JUtils.setVelocity(target, user.getDeltaMovement().scale(0.8).add(forceInFront));
        }
    }

    @Override
    public void onUserMoveInput(final RangerSpec attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {
        if (type.getMoveClass() == getMoveClass() && !pressed) {
            attacker.cancelMove();
        }
    }

    @Override
    public @NonNull MoveType<RangerSlideMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull RangerSlideMove getThis() {
        return this;
    }

    @Override
    public @NonNull RangerSlideMove copy() {
        return copyExtras(new RangerSlideMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<RangerSlideMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RangerSlideMove>, RangerSlideMove> buildCodec(RecordCodecBuilder.Instance<RangerSlideMove> instance) {
            return attackDefault(instance, RangerSlideMove::new);
        }
    }
}
