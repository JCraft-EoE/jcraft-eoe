package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Set;

@Getter
public class RekkaAttack extends AbstractSimpleAttack<RekkaAttack, GoldExperienceEntity> {
    private final int rekkaLevel;
    private final RekkaAttack next;
    private final int switchStart;
    private final GoldExperienceEntity.State nextState;

    public RekkaAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitBoxSize,
                       float knockBack, float offset, int rekkaLevel, int switchStart, RekkaAttack next, GoldExperienceEntity.State nextState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitBoxSize, knockBack, offset);
        if (rekkaLevel > 1) hitSpark = JParticleType.HIT_SPARK_2;
        this.rekkaLevel = rekkaLevel;
        this.switchStart = switchStart;
        this.next = next;
        this.nextState = nextState;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GoldExperienceEntity stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);

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
    protected RekkaAttack getThis() {
        return this;
    }
}
