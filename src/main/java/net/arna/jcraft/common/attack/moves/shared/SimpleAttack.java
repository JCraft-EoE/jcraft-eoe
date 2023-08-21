package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.util.JParticleType;

public class SimpleAttack<A extends IAttacker<?, ?>> extends AbstractSimpleAttack<SimpleAttack<A>, A> {
    /**
     * Creates a new simple attack with a single hitbox.
     * @param cooldown The cooldown for this attack in ticks.
     * @param windup The windup of this attack in ticks. How long until the blow is landed.
     * @param duration The duration after which a new attack can be initiated in ticks.
     * @param damage The damage this attack deals.
     * @param hitboxSize The size of the hitbox in blocks.
     * @param knockback The strength of the knock-back.
     * @param attackDistance The distance at which the hitbox is placed.
     * @param offset The amount the hitbox is offset by.
     */
    public SimpleAttack(int cooldown, int windup, int duration, float damage, int stun, float hitboxSize, float knockback, float attackDistance, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
    }

    /**
     * For light attacks
     * @param windup The windup of this attack in ticks. How long until the blow is landed.
     * @param duration The duration after which a new attack can be initiated in ticks.
     * @param damage The damage this attack deals.
     * @param attackDistance The distance at which the hitbox is placed.
     * @param offset The amount the hitbox is offset by.
     */
    public static <A extends IAttacker<?, ?>> SimpleAttack<A> lightAttack(int windup, int duration, float damage,
                                                                            int stun, float knockback, float attackDistance, float offset) {
        return new SimpleAttack<>(30, windup, duration, damage, stun, 1.5f, knockback, attackDistance, offset);
    }

    public SimpleAttack<A> withHitSpark(JParticleType particle) {
        this.hitSpark = particle;
        return this;
    }

    @Override
    protected @NonNull SimpleAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SimpleAttack<A> copy() {
        return copyExtras(new SimpleAttack<>(getCooldown(), getWindup(), getDuration(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getMoveDistance(), getOffset()));
    }
}
