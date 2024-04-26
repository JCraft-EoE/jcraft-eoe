package net.arna.jcraft.common.attack.moves.anubis;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.common.spec.AnubisSpec;

public class SweepFollowupAttack extends KnockdownAttack<AnubisSpec> {
    public SweepFollowupAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset, int knockdownDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, knockdownDuration);
    }



    @Override
    protected @NonNull SweepFollowupAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SweepFollowupAttack copy() {
        return copyExtras(
                new SweepFollowupAttack(getCooldown(), getWindup(), getDuration(),
                        getMoveDistance(), getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getKnockdownDuration())
        );
    }
}
