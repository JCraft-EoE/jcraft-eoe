package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;

public class LungeAttack extends AbstractSimpleAttack<LungeAttack, TheWorldOverHeavenEntity> {
    private final float originalMoveDistance;

    public LungeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                       float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        originalMoveDistance = moveDistance;
        this.ranged = true;
    }

    @Override
    public void onInitiate(TheWorldOverHeavenEntity attacker) {
        super.onInitiate(attacker);

        // Reset move distance
        withMoveDistance(originalMoveDistance);
    }

    @Override
    public void tick(TheWorldOverHeavenEntity attacker) {
        super.tick(attacker);

        int moveStun = attacker.getMoveStun();
        if (moveStun > 5 && moveStun <= 11)
            withMoveDistance(getMoveDistance() + 0.15f);
    }

    @Override
    protected @NonNull LungeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull LungeAttack copy() {
        return copyExtras(new LungeAttack(getCooldown(), getWindup(), getDuration(), originalMoveDistance, getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
