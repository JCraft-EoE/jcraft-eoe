package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class SpinBarrageAttack extends AbstractBarrageAttack<SpinBarrageAttack, SilverChariotEntity> {
    public SpinBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        // TODO make this more interesting. S.a. deflecting projectiles and launch stun
        return super.perform(attacker, user, ctx);
    }

    @Override
    protected @NonNull SpinBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SpinBarrageAttack copy() {
        return new SpinBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getInterval());
    }
}
