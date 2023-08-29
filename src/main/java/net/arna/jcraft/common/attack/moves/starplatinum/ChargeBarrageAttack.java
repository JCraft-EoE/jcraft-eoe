package net.arna.jcraft.common.attack.moves.starplatinum;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StarPlatinumEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class ChargeBarrageAttack extends AbstractBarrageAttack<ChargeBarrageAttack, StarPlatinumEntity> {
    public ChargeBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                  float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        charge = true;
        ranged = true;
        inflictsSlowness = false;
    }

    @Override
    public void tick(StarPlatinumEntity attacker) {
        super.tick(attacker);

        AbstractChargeAttack.tickChargeAttack(attacker, shouldPerform(attacker), getMoveDistance(), getWindupPoint());
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
