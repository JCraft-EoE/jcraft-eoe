package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.Set;

@Getter
public abstract class AbstractChargeAttack<T extends AbstractChargeAttack<T, S, A>, S extends StandEntity<S, A>, A extends Enum<A> & StandAnimationState<S>>
        extends AbstractSimpleAttack<T, S> {
    private final A hitAnimState;

    public AbstractChargeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                float hitboxSize, float knockback, float offset, A hitAnimState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.hitAnimState = hitAnimState;
        charge = true;
        ranged = true;
    }

    @Override
    protected boolean shouldPerform(S stand) {
        return hasWindupPassed(stand);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(S stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);

        if (!targets.isEmpty()) {
            stand.curMove = null;
            stand.setMoveStun(10);
            stand.setState(hitAnimState);
        }

        return targets;
    }

    @Override
    public void tick(S stand) {
        super.tick(stand);

        if (shouldPerform(stand)) {
            //float t = 1f - (float) curMoveStun / (float) realInitTime;
            Vec3d newPos = stand.getPos().add(getRotVec(stand).multiply(getMoveDistance() / getWindupPoint()));
            //stand.setDistanceOffset(1 + attackDist * t * t);
            stand.setFreePos(new Vec3f((float) newPos.x, (float) newPos.y, (float) newPos.z));
            stand.setFree(true);
        } else {
            stand.setPosition(stand.getUserOrThrow().getPos());
            stand.setRotationOffset(stand.attackRotation);
        }
    }

    @Override
    protected Vec3d getOffsetForwardPos(S stand, Vec3d offsetHeightPos, Vec3d upVec, Vec3d rotVec) {
        return offsetHeightPos.add(rotVec);
    }
}
