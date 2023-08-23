package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class PoundAttack extends AbstractSimpleAttack<PoundAttack, TheFoolEntity> {
    public PoundAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                       float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        for (LivingEntity target : targets) {
            Vec3d vel = target.getVelocity();
            target.setVelocity(vel.x, (attacker.getMoveStun() > 14) ? 0.5 : -1, vel.y);
            target.velocityModified = true;
        }

        return targets;
    }

    @Override
    protected @NonNull PoundAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PoundAttack copy() {
        return copyExtras(new PoundAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
