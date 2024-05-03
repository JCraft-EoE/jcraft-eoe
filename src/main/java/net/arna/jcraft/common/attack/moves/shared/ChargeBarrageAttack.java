package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Set;

import static net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack.prepDetachmentMove;

public class ChargeBarrageAttack<A extends IAttacker<? extends A, ?>> extends AbstractBarrageAttack<ChargeBarrageAttack<A>, A> {
    private final float originalMoveDistance;
    private final boolean quadraticMovement;

    public ChargeBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                               float hitboxSize, float knockback, float offset, int interval, boolean quadraticMovement) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.originalMoveDistance = moveDistance;
        this.quadraticMovement = quadraticMovement;

        withCopyOnUse();
        withStunType(StunType.BURSTABLE);
        charge = true;
        ranged = true;
        inflictsSlowness = false;
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        withMoveDistance(originalMoveDistance);
    }

    @Override
    public void tick(A attacker) {
        super.tick(attacker);
        Entity attackerEntity = attacker.getBaseEntity();
        if (attackerEntity instanceof StandEntity<?,?> stand)
            tickChargeBarrageAttack(stand, attacker.getMoveStun() < getWindupPoint(), getMoveDistance(), getWindupPoint());
        else
            JCraft.LOGGER.error("Trying to tick ChargeBarrageAttack non non-stand entity; " + attackerEntity);
    }

    protected Vec3d advanceChargePos(StandEntity<?, ?> attacker, float moveDistance, int windupPoint) {
        if (quadraticMovement)
            return attacker.getPos().add(getRotVec(attacker).multiply(
                    (moveDistance * attacker.getMoveStun()) / (windupPoint * windupPoint)
            ));
        return attacker.getPos().add(getRotVec(attacker).multiply(moveDistance / windupPoint));
    }

    protected void tickChargeBarrageAttack(StandEntity<?, ?> attacker, boolean shouldPerform, float moveDistance, int windupPoint) {
        if (shouldPerform) {
            Vec3d newPos = advanceChargePos(attacker, moveDistance, windupPoint);
            attacker.setFreePos(new Vector3f((float) newPos.x, (float) newPos.y, (float) newPos.z));
            attacker.setFree(true);
        } else prepDetachmentMove(attacker, attacker.getUserOrThrow());
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        Entity attackerEntity = attacker.getBaseEntity();
        if (targets.isEmpty() || attackerEntity == null) return targets;

        Vec3d avgPos = Vec3d.ZERO;
        float c = 0;
        for (LivingEntity target : targets) {
            if (target instanceof StandEntity<?, ?>) continue;
            avgPos = avgPos.add(target.getPos());
            c += 1f;
        }
        avgPos = avgPos.multiply(1f / c);
        attackerEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, avgPos);
        withMoveDistance((float) avgPos.squaredDistanceTo(attackerEntity.getPos()) + 0.1f);

        return targets;
    }

    @Override
    protected Vec3d getOffsetForwardPos(A attacker, Vec3d offsetHeightPos, Vec3d upVec, Vec3d rotVec) {
        return offsetHeightPos.add(rotVec);
    }

    @Override
    protected @NonNull ChargeBarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ChargeBarrageAttack<A> copy() {
        return copyExtras(new ChargeBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), quadraticMovement));
    }
}
