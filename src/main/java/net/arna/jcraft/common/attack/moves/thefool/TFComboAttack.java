package net.arna.jcraft.common.attack.moves.thefool;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Set;

public class TFComboAttack extends AbstractMultiHitAttack<TFComboAttack, TheFoolEntity> {
    public TFComboAttack(int cooldown, int duration, float moveDistance, float damage, int stun, float hitboxSize,
                         float knockback, float offset, @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (getBlow(attacker) == 2) for (LivingEntity ent : targets)
            ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 20, 0, true, false));

        return targets;
    }

    @Override
    protected @NonNull TFComboAttack getThis() {
        return this;
    }

    @Override
    public @NonNull TFComboAttack copy() {
        return copyExtras(new TFComboAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }
}
