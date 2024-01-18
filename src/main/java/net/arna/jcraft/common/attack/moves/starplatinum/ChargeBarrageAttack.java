package net.arna.jcraft.common.attack.moves.starplatinum;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StarPlatinumEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.Set;

public class ChargeBarrageAttack extends AbstractBarrageAttack<ChargeBarrageAttack, StarPlatinumEntity> {
    private final float originalMoveDistance;

    public ChargeBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                               float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.originalMoveDistance = moveDistance;
        this.withStunType(StunType.BURSTABLE);
        charge = true;
        ranged = true;
        inflictsSlowness = false;
    }

    @Override
    public void onInitiate(StarPlatinumEntity attacker) {
        super.onInitiate(attacker);

        withMoveDistance(originalMoveDistance);
    }

    @Override
    public void tick(StarPlatinumEntity attacker) {
        super.tick(attacker);

        tickChargeBarrageAttack(attacker, shouldPerform(attacker), getMoveDistance(), getWindupPoint());
    }

    protected Vec3d advanceChargePos(StandEntity<?, ?> attacker, float moveDistance, int windupPoint) {
        return attacker.getPos().add(getRotVec(attacker).multiply(moveDistance / windupPoint));
    }

    protected void tickChargeBarrageAttack(StandEntity<?, ?> attacker, boolean shouldPerform, float moveDistance, int windupPoint) {
        if (shouldPerform) {
            Vec3d newPos = advanceChargePos(attacker, moveDistance, windupPoint);
            attacker.setFreePos(new Vec3f((float) newPos.x, (float) newPos.y, (float) newPos.z));
            attacker.setFree(true);
        } else {
            attacker.setPosition(attacker.getUserOrThrow().getPos());
            attacker.setRotationOffset(attacker.attackRotation);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(StarPlatinumEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return targets;

        Vec3d avgPos = Vec3d.ZERO;
        float c = 0;
        for (LivingEntity target : targets) {
            if (target instanceof StandEntity<?, ?>) continue;
            avgPos = avgPos.add(target.getPos());
            c += 1f;
        }
        avgPos = avgPos.multiply(1f / c);
        attacker.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, avgPos);
        withMoveDistance((float) avgPos.distanceTo(attacker.getPos()));

        return targets;
    }

    @Override
    protected Vec3d getOffsetForwardPos(StarPlatinumEntity attacker, Vec3d offsetHeightPos, Vec3d upVec, Vec3d rotVec) {
        return offsetHeightPos.add(rotVec);
    }

    @Override
    protected @NonNull ChargeBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ChargeBarrageAttack copy() {
        return copyExtras(new ChargeBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }
}
