package net.arna.jcraft.common.attack.moves.theworld;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheWorldEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class TWDonutAttack extends AbstractSimpleAttack<TWDonutAttack, TheWorldEntity> {
    public TWDonutAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheWorldEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        // If missed, stun the user for 1.5 seconds
        if (targets.isEmpty()) StandEntity.stun(user, 30, 0);
            /* If hit, impale and set position to middle of arm
        else for (LivingEntity entity : entities) {
            Vec3d pos = this.getPos().add(this.getRotationVector().multiply(1.5));
            entity.teleport(pos.x, entity.getY(), pos.z);
        }*/

        return targets;
    }

    @Override
    protected @NonNull TWDonutAttack getThis() {
        return this;
    }

    @Override
    public @NonNull TWDonutAttack copy() {
        return copyExtras(new TWDonutAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
