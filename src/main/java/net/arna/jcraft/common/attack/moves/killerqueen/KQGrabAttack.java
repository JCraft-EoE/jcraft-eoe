package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractGrabAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class KQGrabAttack extends AbstractGrabAttack<KQGrabAttack, KillerQueenEntity, KillerQueenEntity.State> {
    public KQGrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                        float knockback, float offset, AbstractMove<?, ? super KillerQueenEntity> hitMove, KillerQueenEntity.State hitState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KillerQueenEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        targets.stream().findFirst().ifPresent(JComponents.getBombTracker(user).getMainBomb()::setBomb);
        return targets;
    }

    @Override
    protected @NonNull KQGrabAttack getThis() {
        return this;
    }

    @Override
    public @NonNull KQGrabAttack copy() {
        return copyExtras(new KQGrabAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMove(), getHitState()));
    }
}
