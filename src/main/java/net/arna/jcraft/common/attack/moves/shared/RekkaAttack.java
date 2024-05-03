package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.Set;

@Getter
public class RekkaAttack<A extends IAttacker<A, S> & GeoEntity, S extends Enum<S> & StandAnimationState<A>>
        extends AbstractSimpleAttack<RekkaAttack<A, S>, A> {
    private final int rekkaLevel;
    private final RekkaAttack<A, S> next;
    private final int switchStart;
    private final StandAnimationState<A> nextState;

    public RekkaAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                       float knockback, float offset, int rekkaLevel, int switchStart, RekkaAttack<A, S> next, StandAnimationState<A> nextState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        if (rekkaLevel > 1) hitSpark = JParticleType.HIT_SPARK_2;
        this.rekkaLevel = rekkaLevel;
        this.switchStart = switchStart;
        this.next = next;
        this.nextState = nextState;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (rekkaLevel == 3)
            for (LivingEntity target : targets)
                if (!JUtils.isBlocking(target))
                    target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 50, 0, true, false));

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
