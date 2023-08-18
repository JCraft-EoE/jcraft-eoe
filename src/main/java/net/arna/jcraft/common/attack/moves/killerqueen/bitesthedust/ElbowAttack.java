package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import net.arna.jcraft.common.attack.core.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ElbowAttack extends AbstractSimpleAttack<ElbowAttack, KQBTDEntity> {
    public ElbowAttack(int cooldown, int windup, int moveStunTicks, float attackDistance, float damage, float hitBoxSize, float knockBack, float offset) {
        super(cooldown, windup, moveStunTicks, attackDistance, damage, hitBoxSize, knockBack, offset);
        withLaunch();
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NotNull Set<LivingEntity> perform(KQBTDEntity stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);
        for (LivingEntity target : targets)
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 4, true, false));

        return targets;
    }

    @Override
    protected ElbowAttack getThis() {
        return this;
    }
}
