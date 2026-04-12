package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
public final class RekkaAttack<A extends IAttacker<A, S>, S extends Enum<S> & StandAnimationState<A>>
        extends AbstractSimpleAttack<RekkaAttack<A, S>, A> {
    private final int rekkaLevel;
    private final RekkaAttack<A, S> next;
    private final int switchStart;
    private final StandAnimationState<A> nextState;

    public RekkaAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun, final float hitboxSize,
                       final float knockback, final float offset, final int rekkaLevel, final int switchStart, final RekkaAttack<A, S> next, final StandAnimationState<A> nextState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        if (rekkaLevel > 1) {
            hitSpark = JParticleType.HIT_SPARK_2;
        }
        this.rekkaLevel = rekkaLevel;
        this.switchStart = switchStart;
        this.next = next;
        this.nextState = nextState;
    }

    @Override
    public @NonNull MoveType<RekkaAttack<A, S>> getMoveType() {
        //noinspection DataFlowIssue
        return null; // Class is unused and making a type for this is impossible (sort of).
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        if (rekkaLevel == 3) {
            for (LivingEntity target : targets) {
                if (!JUtils.isBlocking(target)) {
                    target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 50, 0, true, false));
                }
            }
        }

        return targets;
    }

    public boolean mayAdvance(GoldExperienceEntity stand) {
        return stand.getMoveStun() < switchStart;
    }

    @Override
    protected @NonNull RekkaAttack<A, S> getThis() {
        return this;
    }

    @Override
    public @NonNull RekkaAttack<A, S> copy() {
        return copyExtras(new RekkaAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getRekkaLevel(), getSwitchStart(), getNext(), getNextState()));
    }
}
