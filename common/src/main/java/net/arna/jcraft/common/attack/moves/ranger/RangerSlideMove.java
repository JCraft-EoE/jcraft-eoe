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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class RangerSlideMove extends AbstractSimpleAttack<RangerSlideMove, RangerSpec> {
    private static final double SLIDE_SPEED = 0.75;
    private static final int SLIDE_START_TICKS = 12;

    private final Map<RangerSpec, Set<LivingEntity>> carriedTargets = new WeakHashMap<>();
    private final Map<RangerSpec, Vec3> slideVectors = new WeakHashMap<>();

    public RangerSlideMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        mobilityType = MobilityType.DASH;
        withHoldable();
    }

    @Override
    public void onInitiate(final RangerSpec attacker) {
        super.onInitiate(attacker);
        carriedTargets.remove(attacker);
        final LivingEntity user = attacker.getUser();
        if (user != null) {
            slideVectors.put(attacker, createSlideVector(user));
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

        user.setPose(Pose.SWIMMING);

        final Vec3 slideVector = slideVectors.getOrDefault(attacker, Vec3.directionFromRotation(0, user.getYRot()).scale(SLIDE_SPEED));
        final Vec3 delta = user.getDeltaMovement();
        final Vec3 next = delta.add(slideVector).scale(0.5);
        user.setDeltaMovement(next.x, delta.y, next.z);
        user.hurtMarked = true;

        if (getDuration() - moveStun == SLIDE_START_TICKS) {
            attacker.setState(RangerSpec.State.SLIDE_LOOP);
        }

        super.activeTick(attacker, moveStun);
    }

    private Vec3 createSlideVector(final LivingEntity user) {
        int forward = 1;
        int side = 0;
        if (user instanceof ServerPlayer player) {
            final InputStateManager input = PlayerInputPacket.getInputStateManager(player);
            forward = input.calcForward();
            side = input.calcSide();
            if (forward == 0 && side == 0) {
                forward = 1;
            }
        }
        double speed = SLIDE_SPEED;
        if (side != 0) {
            speed *= 0.75;
        }
        if (forward == -1) {
            speed *= 0.75;
        }
        final float angle = (float) Math.atan2(side, forward);
        return Vec3.directionFromRotation(0, user.getYRot()).yRot(angle).scale(speed);
    }

    @Override
    public boolean shouldPerform(final RangerSpec attacker, final int moveStun) {
        return attacker.hasUser();
    }

    @Override
    protected Set<AABB> calculateBoxes(final RangerSpec attacker, final LivingEntity user, final Vec3 rotVec,
                                       final Vec3 upVec, final Vec3 hPos, final Vec3 fPos) {
        final Vec3 slideVector = slideVectors.getOrDefault(attacker, Vec3.ZERO);
        return Set.of(createBox(user.getBoundingBox().getCenter(), getHitboxSize()).expandTowards(slideVector));
    }

    @Override
    protected Set<LivingEntity> validateTargets(final RangerSpec attacker, final Set<LivingEntity> targets) {
        super.validateTargets(attacker, targets);
        final Set<LivingEntity> carried = carriedTargets.computeIfAbsent(attacker, a -> new HashSet<>());
        targets.removeIf(carried::contains);
        carried.addAll(targets);
        return targets;
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
