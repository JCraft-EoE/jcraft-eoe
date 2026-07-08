package net.arna.jcraft.common.attack.moves.ranger;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.spec.RangerSpec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class RangerSlideMove extends AbstractHoldableMove<RangerSlideMove, RangerSpec> {
    private static final float DAMAGE = 3f;
    private static final int STUN = 20;
    private static final double SLIDE_SPEED = 0.5;
    private static final float LAUNCH_STRENGTH = 0.75f;
    // Length of the slide_start animation
    private static final int SLIDE_START_TICKS = 12;

    private final Map<RangerSpec, Set<LivingEntity>> carriedTargets = new WeakHashMap<>();

    public RangerSlideMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int minimumCharge) {
        super(cooldown, windup, duration, moveDistance, minimumCharge);
        mobilityType = MobilityType.DASH;
    }

    @Override
    public void onInitiate(final RangerSpec attacker) {
        super.onInitiate(attacker);
        carriedTargets.remove(attacker);
    }

    @Override
    public void activeTick(final RangerSpec attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);

        final LivingEntity user = attacker.getUser();
        if (user == null || attacker.getCurrentMove() != this) {
            return;
        }

        // The looping slide animation only resets when the move is cancelled
        if (moveStun <= 1) {
            attacker.cancelMove();
            return;
        }

        user.setPose(Pose.SWIMMING); // Pose recalculation is suppressed in PlayerMixin

        final Vec3 slideVector = Vec3.directionFromRotation(0, user.getYRot()).scale(SLIDE_SPEED);
        final Vec3 delta = user.getDeltaMovement();
        final Vec3 next = delta.add(slideVector).scale(0.5);
        user.setDeltaMovement(next.x, delta.y, next.z);
        user.hurtMarked = true;

        if (getDuration() - moveStun == SLIDE_START_TICKS) {
            attacker.setState(RangerSpec.State.SLIDE_LOOP);
        }

        launchTargets(attacker, user, slideVector);
    }

    private void launchTargets(final RangerSpec attacker, final LivingEntity user, final Vec3 slideVector) {
        final DamageSource damageSource = attacker.getDamageSource();
        final AABB box = user.getBoundingBox().inflate(0.25).expandTowards(slideVector);
        final Set<LivingEntity> targets = AbstractSimpleAttack.findHits(attacker, box, damageSource);
        if (targets.isEmpty()) {
            return;
        }

        final Set<LivingEntity> carried = carriedTargets.computeIfAbsent(attacker, a -> new HashSet<>());
        for (final LivingEntity target : targets) {
            if (!carried.add(target)) {
                continue;
            }

            damageLogic(
                    attacker.getEntityWorld(),
                    target,
                    new AttackData(
                            new Vec3(0.0, LAUNCH_STRENGTH, 0.0), STUN, StunType.LAUNCH.ordinal(), true,
                            DAMAGE, true, (int) (DAMAGE + 4), damageSource, user,
                            CommonHitPropertyComponent.HitAnimation.LAUNCH, attacker.getMoveUsage(), false, false, true
                    )
            );
        }
    }

    @Override
    protected void onRelease(final RangerSpec attacker) {
        if (attacker.getMoveStun() <= getDuration() - getMinimumCharge()) {
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
        RangerSlideMove copy = new RangerSlideMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getMinimumCharge());
        if (setMoveStun) {
            copy.shouldSetMoveStun();
        }
        return copyExtras(copy);
    }

    public static class Type extends AbstractHoldableMove.Type<RangerSlideMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RangerSlideMove>, RangerSlideMove> buildCodec(RecordCodecBuilder.Instance<RangerSlideMove> instance) {
            return holdableDefault(instance, RangerSlideMove::new);
        }
    }
}
