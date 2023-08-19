package net.arna.jcraft.common.attack.moves.shared;

import it.unimi.dsi.fastutil.ints.IntCollection;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;

/**
 * The simplest implementation of {@link AbstractMultiHitAttack}.
 * Only special feature is that it fires the same hitbox at set points.
 * @param <S>
 */
public class SimpleMultiHitAttack<S extends StandEntity<?, ?>> extends AbstractMultiHitAttack<SimpleMultiHitAttack<S>, S> {
    public SimpleMultiHitAttack(int cooldown, int duration, float damage, int stun, float hitboxSize, float knockback,
                                float moveDistance, float offset, IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    /**
     * For light attacks
     * @param moveStunTicks The duration after which a new attack can be initiated in ticks.
     * @param damage The damage this attack deals.
     * @param attackDistance The distance at which the hitbox is placed.
     * @param offset The amount the hitbox is offset by.
     * @param hitMoments The ticks at which this attack is performed.
     */
    public static <S extends StandEntity<?, ?>> SimpleMultiHitAttack<S> lightAttack(int moveStunTicks, float damage,
                                                                                    int stun, float attackDistance,
                                                                                    float offset, IntCollection hitMoments) {
        return new SimpleMultiHitAttack<>(30, moveStunTicks, damage, stun, 1.5f, 0.75f,
                attackDistance, offset, hitMoments);
    }

    @Override
    protected SimpleMultiHitAttack<S> getThis() {
        return this;
    }

    @Override
    public SimpleMultiHitAttack<S> copy() {
        return new SimpleMultiHitAttack<>(getCooldown(), getDuration(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getMoveDistance(), getOffset(), getHitMoments());
    }
}
