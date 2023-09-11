package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.killerqueen.DetonateAttack;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Set;

public class BTDGrabHitAttack extends AbstractMultiHitAttack<BTDGrabHitAttack, KQBTDEntity> {
    public BTDGrabHitAttack(int cooldown, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                            float knockback, float offset, @NonNull IntCollection hitMoments) {
        super(cooldown, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KQBTDEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        switch (getBlow(attacker)) {
            case 0 -> {
                for (LivingEntity ent : targets)
                    ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 40, 0, true, false, true));
            }
            case 2 -> DetonateAttack.explode(attacker, user, attacker.getPos().subtract(0, .5, 0));
        }

        return targets;
    }

    @Override
    protected @NonNull BTDGrabHitAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BTDGrabHitAttack copy() {
        return copyExtras(new BTDGrabHitAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getOffset(), getHitMoments()));
    }
}
