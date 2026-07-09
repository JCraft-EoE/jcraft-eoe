package net.arna.jcraft.common.attack.moves.ranger;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.spec.RangerSpec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class RangerRollMove extends AbstractMove<RangerRollMove, RangerSpec> {
    public static final int RECOVERY_TICKS = 4;
    private static final int EXHAUSTION_DURATION = 20 * 3;
    private static final double ROLL_SPEED = 0.5;

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
            rollVectors.put(attacker, Vec3.directionFromRotation(0, user.getYRot()).scale(ROLL_SPEED));
        }
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

            final Vec3 rollVector = rollVectors.get(attacker);
            if (rollVector != null) {
                final Vec3 delta = user.getDeltaMovement();
                final Vec3 next = delta.add(rollVector).scale(0.5);
                user.setDeltaMovement(next.x, delta.y, next.z);
                user.hurtMarked = true;
            }
        }

        if (moveStun == 1) {
            user.addEffect(new MobEffectInstance(JStatusRegistry.EXHAUSTION.get(), EXHAUSTION_DURATION, 1));
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
