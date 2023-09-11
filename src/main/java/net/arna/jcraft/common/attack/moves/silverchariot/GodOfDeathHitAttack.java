package net.arna.jcraft.common.attack.moves.silverchariot;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class GodOfDeathHitAttack extends AbstractMultiHitAttack<GodOfDeathHitAttack, SilverChariotEntity> {
    public GodOfDeathHitAttack(int cooldown, int duration, float moveDistance, float damage, int stun,
                               float hitboxSize, float knockback, float offset, IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (getBlow(attacker) == 1) attacker.curMove = getFollowup();

        return targets;
    }

    @Override
    protected @NonNull GodOfDeathHitAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GodOfDeathHitAttack copy() {
        return copyExtras(new GodOfDeathHitAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }
}
