package net.arna.jcraft.common.attack.moves.ranger;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.network.c2s.PlayerInputPacket;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.common.util.InputStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class RangerRollMove extends AbstractMove<RangerRollMove, RangerSpec> {
    public static final int RECOVERY_TICKS = 4;
    private static final double ROLL_SPEED = 0.5;
    private static final float AUTOSTEP_HEIGHT = 1.0f; // roll straight up full blocks
    private static final float DEFAULT_STEP_HEIGHT = 0.6f; // vanilla player/mob step height

    private final Map<RangerSpec, Vec3> rollVectors = new WeakHashMap<>();

    public RangerRollMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.DASH;
    }

    @Override
    public void onInitiate(final RangerSpec attacker) {
        super.onInitiate(attacker);
        final LivingEntity user = attacker.getUser();
        if (user != null) {
            rollVectors.put(attacker, createRollVector(user));
        }
    }

    private static Vec3 createRollVector(final LivingEntity user) {
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

        double speed = ROLL_SPEED;

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
    public void activeTick(final RangerSpec attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);

        final LivingEntity user = attacker.getUser();
        if (user == null) {
            return;
        }

        user.fallDistance = 0;

        if (moveStun > RECOVERY_TICKS) {
            // user.setPose(Pose.SWIMMING); // Pose recalculation is suppressed in PlayerMixin

            user.setMaxUpStep(AUTOSTEP_HEIGHT);

            final Vec3 rollVector = rollVectors.get(attacker);
            if (rollVector != null) {
                final Vec3 delta = user.getDeltaMovement();
                final Vec3 next = delta.add(rollVector).scale(0.5);
                user.setDeltaMovement(next.x, delta.y, next.z);
                user.hurtMarked = true;
            }
        } else {
            user.setMaxUpStep(DEFAULT_STEP_HEIGHT);
        }
    }

    @Override
    public void onDeactivate(final RangerSpec attacker) {
        super.onDeactivate(attacker);
        final LivingEntity user = attacker.getUser();
        if (user != null) {
            user.setMaxUpStep(DEFAULT_STEP_HEIGHT);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final RangerSpec attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public @NonNull MoveType<RangerRollMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull RangerRollMove getThis() {
        return this;
    }

    @Override
    public @NonNull RangerRollMove copy() {
        return copyExtras(new RangerRollMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<RangerRollMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RangerRollMove>, RangerRollMove> buildCodec(RecordCodecBuilder.Instance<RangerRollMove> instance) {
            return baseDefault(instance, RangerRollMove::new);
        }
    }
}
