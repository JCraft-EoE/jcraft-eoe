package net.arna.jcraft.common.attack.moves.cream;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class CreamComboAttack extends AbstractMultiHitAttack<CreamComboAttack, CreamEntity> {
    public CreamComboAttack(int cooldown, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset,
                            @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CreamEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (getBlow(attacker) == 2) {
            Vec3d rV = getRotVec(attacker);

            for (LivingEntity target : targets) {
                target.takeKnockback(1, rV.x, rV.z);
                target.velocityModified = true;
            }
        }

        return targets;
    }

    @Override
    protected @NonNull CreamComboAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CreamComboAttack copy() {
        return copyExtras(new CreamComboAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }
}
