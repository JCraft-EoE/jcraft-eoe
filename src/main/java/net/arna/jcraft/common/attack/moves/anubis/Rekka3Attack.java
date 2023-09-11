package net.arna.jcraft.common.attack.moves.anubis;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class Rekka3Attack extends AbstractMultiHitAttack<Rekka3Attack, AnubisSpec> {
    public Rekka3Attack(int cooldown, int duration, float moveDistance, float damage, int stun, float hitboxSize,
                        float knockback, float offset, @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AnubisSpec attacker, LivingEntity user, MoveContext ctx) {
        if (attacker.getAttackSpeedMult() == 1 && getBlow(attacker) == 1) attacker.curMove = getFollowup();

        return super.perform(attacker, user, ctx);
    }

    @Override
    protected @NonNull Rekka3Attack getThis() {
        return this;
    }

    @Override
    public @NonNull Rekka3Attack copy() {
        return copyExtras(new Rekka3Attack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getOffset(), getHitMoments()));
    }
}
