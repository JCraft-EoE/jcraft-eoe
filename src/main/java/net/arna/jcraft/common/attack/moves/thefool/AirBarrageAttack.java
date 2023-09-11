package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.minecraft.entity.LivingEntity;

public class AirBarrageAttack extends AbstractBarrageAttack<AirBarrageAttack, TheFoolEntity> {
    public AirBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public void tick(TheFoolEntity attacker) {
        super.tick(attacker);

        if (!attacker.hasUser()) return;

        LivingEntity user = attacker.getUserOrThrow();
        user.setVelocity(user.getVelocity().multiply(0.5).add(0, 0.01, 0));
        user.velocityModified = true;
    }

    @Override
    protected @NonNull AirBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull AirBarrageAttack copy() {
        return copyExtras(new AirBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }
}
