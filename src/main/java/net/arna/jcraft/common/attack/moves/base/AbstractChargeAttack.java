package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Set;

@Getter
public abstract class AbstractChargeAttack<T extends AbstractChargeAttack<T, A, S>, A extends StandEntity<A, S>, S extends Enum<S> & StandAnimationState<A>>
        extends AbstractSimpleAttack<T, A> {
    private final S hitAnimState;

    protected AbstractChargeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                float hitboxSize, float knockback, float offset, S hitAnimState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);

        this.hitAnimState = hitAnimState;

        charge = true;
        ranged = true;

        // Charge attacks can't backstab
        this.withBackstab(false);
    }

    @Override
    protected boolean shouldPerform(A attacker) {
        return hasWindupPassed(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (!targets.isEmpty()) {
            attacker.setCurrentMove(null);
            attacker.setMoveStun(10);
            attacker.setState(hitAnimState);
        }

        return targets;
    }

    @Override
    public void tick(A attacker) {
        super.tick(attacker);

        tickChargeAttack(attacker, shouldPerform(attacker), getMoveDistance(), getWindupPoint());
    }

    protected Vec3d advanceChargePos(StandEntity<?, ?> attacker, float moveDistance, int windupPoint) {
        return attacker.getPos().add(getRotVec(attacker).multiply(moveDistance / windupPoint));
    }

    protected void tickChargeAttack(StandEntity<?, ?> attacker, boolean shouldPerform, float moveDistance, int windupPoint) {
        if (shouldPerform) {
            //float t = 1f - (float) curMoveStun / (float) realInitTime;
            Vec3d newPos = advanceChargePos(attacker, moveDistance, windupPoint);
            //stand.setDistanceOffset(1 + attackDist * t * t);
            attacker.setFreePos(new Vector3f((float) newPos.x, (float) newPos.y, (float) newPos.z));
            attacker.setFree(true);
        } else prepDetachmentMove(attacker, attacker.getUserOrThrow());
    }

    public static void prepDetachmentMove(StandEntity<?, ?> attacker, LivingEntity user) {
        attacker.setPosition(user.getPos());
        attacker.setHeadYaw(user.getHeadYaw());
        attacker.setBodyYaw(user.getHeadYaw());
        attacker.setRotationOffset(attacker.attackRotation);
    }

    @Override
    protected Vec3d getOffsetForwardPos(A attacker, Vec3d offsetHeightPos, Vec3d upVec, Vec3d rotVec) {
        return offsetHeightPos.add(rotVec);
    }
}
