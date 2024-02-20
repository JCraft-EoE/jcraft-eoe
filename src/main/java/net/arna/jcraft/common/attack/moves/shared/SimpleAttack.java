package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;

public class SimpleAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<SimpleAttack<A>, A> {
    /**
     * Creates a new simple attack with a single hitbox.
     *
     * @param cooldown     The cooldown for this attack in ticks.
     * @param windup       The windup of this attack in ticks. How long until the blow is landed.
     * @param duration     The duration after which a new attack can be initiated in ticks.
     * @param moveDistance The distance at which the hitbox is placed.
     * @param damage       The damage this attack deals.
     * @param hitboxSize   The size of the hitbox in blocks.
     * @param knockback    The strength of the knock-back.
     * @param offset       The amount the hitbox is offset by.
     */
    public SimpleAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                        float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    /**
     * For light attacks
     *
     * @param windup       The windup of this attack in ticks. How long until the blow is landed.
     * @param duration     The duration after which a new attack can be initiated in ticks.
     * @param moveDistance The distance at which the hitbox is placed.
     * @param damage       The damage this attack deals.
     * @param offset       The amount the hitbox is offset by.
     */
    public static <A extends IAttacker<? extends A, ?>> SimpleAttack<A> lightAttack(int windup, int duration, float moveDistance, float damage, int stun,
                                                                                    float knockback, float offset) {
        return new SimpleAttack<>(JCraft.LIGHT_COOLDOWN, windup, duration, moveDistance, damage, stun, 1.5f, knockback, offset);
    }

    @Override
    protected @NonNull SimpleAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SimpleAttack<A> copy() {
        return copyExtras(new SimpleAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
