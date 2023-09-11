package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class GodOfDeathAttack extends AbstractSimpleAttack<GodOfDeathAttack, SilverChariotEntity> {
    public GodOfDeathAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.SWEEP_ATTACK;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (targets.isEmpty()) StandEntity.stun(user, 30, 1);
        else attacker.setMove(getFollowup(), SilverChariotEntity.State.BEAT_DOWN);

        return targets;
    }

    @Override
    protected @NonNull GodOfDeathAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GodOfDeathAttack copy() {
        return copyExtras(new GodOfDeathAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
