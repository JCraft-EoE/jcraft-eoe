package net.arna.jcraft.common.attack.moves.cream;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class ConsumeAttack extends AbstractSimpleAttack<ConsumeAttack, CreamEntity> {
    public ConsumeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CreamEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        attacker.setVoidTime(120);
        attacker.setCharging(false);
        attacker.setCurrentMove(null);

        return targets;
    }

    @Override
    protected @NonNull ConsumeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ConsumeAttack copy() {
        return copyExtras(new ConsumeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
