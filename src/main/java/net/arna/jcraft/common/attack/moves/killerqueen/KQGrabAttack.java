package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractGrabAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class KQGrabAttack extends AbstractGrabAttack<KQGrabAttack, KillerQueenEntity, KillerQueenEntity.State> {
    public KQGrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitBoxSize,
                        float knockBack, float offset, AbstractMove<?, KillerQueenEntity> hitMove, KillerQueenEntity.State hitState) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitBoxSize, knockBack, offset, hitMove, hitState);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KillerQueenEntity stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);

        ctx.set(BombPlantAttack.BOMB_ENTITY, targets.stream().findFirst().orElseThrow());
        ctx.set(BombPlantAttack.BOMB_POS, null);

        return targets;
    }

    @Override
    protected KQGrabAttack getThis() {
        return this;
    }

    @Override
    public KQGrabAttack copy() {
        return new KQGrabAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitBoxSize(), getKnockBack(), getOffset(), getHitMove(), getHitState());
    }
}
