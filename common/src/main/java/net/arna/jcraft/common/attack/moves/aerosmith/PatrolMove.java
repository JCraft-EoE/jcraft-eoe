package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
public class PatrolMove<A extends StandEntity<? extends A, ?>> extends AbstractMove<PatrolMove<A>, A> {

    private float range;
    private float radius;
    private float speed;
    private boolean onPatrol;
    private float actualSpeed;
    private Vec3[] positions;
    private boolean gettingIntoPosition;
    private int currentGoalIndex;
    private boolean goingBack;

    public PatrolMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float range, final float radius, final float speed) {
        super(cooldown, windup, duration, moveDistance);

        withRange(range);
        withRadius(radius);
        withSpeed(speed);
    }

    public PatrolMove<A> withRange(final float range) {
        this.range = range;
        return getThis();
    }

    public PatrolMove<A> withRadius(final float radius) {
        this.radius = radius;
        return getThis();
    }

    public PatrolMove<A> withSpeed(final float speed) {
        this.speed = speed;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        if (user == null) {
            return Set.of();
        }
        onPatrol = !onPatrol;
        attacker.setRemote(onPatrol);
        if (onPatrol) {
            calculateTrajectory(attacker, user);
            goingBack = false;
            gettingIntoPosition = true;
            currentGoalIndex = 0;
            attacker.setDeltaMovement(positions[currentGoalIndex].subtract(attacker.position()).normalize().scale(actualSpeed));
        }
        else {
            gettingIntoPosition = false;
            goingBack = true;
        }
        return Set.of();
    }

    private void calculateTrajectory(final A attacker, final @NonNull LivingEntity user) {
        final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult goal = JUtils.raycastAll(user, userEyePos, userEyePos.add(rotVec.scale(getRange())), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));
        final float circumference = (float)(2 * Math.PI * radius);
        final float durationFloat = circumference / speed;
        final int duration = Math.max((int)Math.floor(durationFloat), 1);
        actualSpeed = circumference / duration;
        positions = new Vec3[duration];
        final double altitude = goal.getLocation().y() + 10d;
        for (int i = 0; i < duration; i++) {
            final double x = goal.getLocation().x() + radius * Math.cos(2*Math.PI / duration * i);
            final double z = goal.getLocation().z() + radius * Math.sin(2*Math.PI / duration * i);
            positions[i] = new Vec3(x, altitude, z);
        }
    }

    @Override
    public void tick(final A attacker) {
        if (gettingIntoPosition && attacker.position().distanceTo(positions[0]) <= 1) {
            gettingIntoPosition = false;
        }
        if (goingBack) {
            if (!attacker.hasUser()) {
                return;
            }
            LivingEntity user = attacker.getUserOrThrow();
            if (attacker.position().distanceTo(user.getEyePosition()) <= 1) {
                goingBack = false;
                attacker.setDeltaMovement(Vec3.ZERO);
                attacker.setRemote(false);
                positions = null;
            }
            else {
                attacker.setDeltaMovement(user.getEyePosition().subtract(attacker.position()).normalize().scale(actualSpeed));
            }
        }
        else if (positions != null) {
            if (attacker.position().distanceTo(positions[currentGoalIndex]) <= 1) {
                currentGoalIndex = (currentGoalIndex + 1) % positions.length;
            }
            final Vec3 diff = positions[currentGoalIndex].subtract(attacker.position());
            attacker.setDeltaMovement(diff.normalize().scale(actualSpeed));
            attacker.setYRot(Mth.wrapDegrees(-(float)Math.atan2(diff.x(), diff.z()) * 57.2957763671875f));
        }
    }

    @Override
    public @NonNull MoveType<PatrolMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull PatrolMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull PatrolMove<A> copy() {
        return copyExtras(new PatrolMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getRange(), getRadius(), getSpeed()));
    }

    public static class Type extends AbstractMove.Type<PatrolMove<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<PatrolMove<?>, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(PatrolMove::getRange);
        }
        protected RecordCodecBuilder<PatrolMove<?>, Float> radius() {
            return Codec.FLOAT.fieldOf("radius").forGetter(PatrolMove::getRadius);
        }
        protected RecordCodecBuilder<PatrolMove<?>, Float> speed() {
            return Codec.FLOAT.fieldOf("speed").forGetter(PatrolMove::getSpeed);
        }

        protected Products.P8<RecordCodecBuilder.Mu<PatrolMove<?>>, BaseMoveExtras, Integer, Integer, Integer, Float, Float, Float, Float>
        bombDefault(RecordCodecBuilder.Instance<PatrolMove<?>> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), range(), radius(), speed());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PatrolMove<?>>, PatrolMove<?>> buildCodec(final RecordCodecBuilder.Instance<PatrolMove<?>> instance) {
            return bombDefault(instance).apply(instance, applyExtras(PatrolMove::new));
        }
    }
}
