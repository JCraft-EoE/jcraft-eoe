package net.arna.jcraft.common.attack.moves.whitesnake;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;

public class PoisonSpewAttack extends AbstractSimpleAttack<PoisonSpewAttack, WhiteSnakeEntity> {
    public PoisonSpewAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected @NonNull PoisonSpewAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PoisonSpewAttack copy() {
        return copyExtras(new PoisonSpewAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
