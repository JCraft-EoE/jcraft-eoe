package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Set;

public class ElbowAttack extends AbstractSimpleAttack<ElbowAttack, KQBTDEntity> {
    public ElbowAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                       float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
        withLaunch();
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KQBTDEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        for (LivingEntity target : targets)
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 5, 4, true, false));

        return targets;
    }

    @Override
    protected @NonNull ElbowAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ElbowAttack copy() {
        return copyExtras(new ElbowAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
