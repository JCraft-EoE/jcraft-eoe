package net.arna.jcraft.common.attack.moves.shared;

import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JParticleType;

public class SimpleAttack<S extends StandEntity<?, ?>> extends AbstractSimpleAttack<SimpleAttack<S>, S> {
    /**
     * Creates a new simple attack with a single hit-box.
     * @param cooldown The cooldown for this attack in ticks.
     * @param windup The windup of this attack in ticks. How long until the blow is landed.
     * @param duration The duration after which a new attack can be initiated in ticks.
     * @param damage The damage this attack deals.
     * @param hitBoxSize The size of the hit-box in blocks.
     * @param knockBack The strength of the knock-back.
     * @param attackDistance The distance at which the hit-box is placed.
     * @param offset The amount the hit-box is offset by.
     */
    public SimpleAttack(int cooldown, int windup, int duration, float damage, float hitBoxSize, float knockBack, float attackDistance, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, hitBoxSize, knockBack, offset);
    }

    public SimpleAttack<S> withHitSpark(JParticleType particle) {
        this.hitSpark = particle;
        return this;
    }

    /**
     * For light attacks
     * @param windup The windup of this attack in ticks. How long until the blow is landed.
     * @param duration The duration after which a new attack can be initiated in ticks.
     * @param damage The damage this attack deals.
     * @param attackDistance The distance at which the hit-box is placed.
     * @param offset The amount the hit-box is offset by.
     */
    public static <S extends StandEntity<?, ?>> SimpleAttack<S> lightAttack(int windup, int duration, float damage, float attackDistance, float offset) {
        return new SimpleAttack<>(30, windup, duration, damage, 1.5f, 0.75f, attackDistance, offset);
    }

    @Override
    protected SimpleAttack<S> getThis() {
        return this;
    }
}
