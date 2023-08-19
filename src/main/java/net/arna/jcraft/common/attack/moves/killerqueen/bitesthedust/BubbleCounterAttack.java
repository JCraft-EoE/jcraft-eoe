package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class BubbleCounterAttack extends AbstractCounterAttack<BubbleCounterAttack, KQBTDEntity> {
    private static final CounterMissAttack<KQBTDEntity> missAttack = new CounterMissAttack<>(15);

    public BubbleCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull KQBTDEntity stand, @NonNull LivingEntity user) {
        stand.setAttack(missAttack, KQBTDEntity.State.COUNTER_MISS);
        StandEntity.stun(user, missAttack.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull KQBTDEntity stand, Entity countered, DamageSource counteredDamageSource) {
        super.counter(stand, countered, counteredDamageSource);
        if (countered == null || !stand.hasUser() || counteredDamageSource.isMagic()) return;

        if (countered instanceof LivingEntity livingEntity) {
            StandEntity.stun(livingEntity, 10, 3);

            StandEntity<?, ?> counteredStand = JUtils.getStand(livingEntity);
            if (counteredStand != null)
                counteredStand.cancelAttack();
        }

        stand.getMoveContext().set(BombPlantAttack.BOMB_ENTITY, countered);
        stand.getMoveContext().set(BombPlantAttack.BOMB_POS, null);
        //stand.playSound(JSoundRegistry.BTD_COUNTER_HIT, 1, 1);
    }

    @Override
    protected BubbleCounterAttack getThis() {
        return this;
    }

    @Override
    public BubbleCounterAttack copy() {
        return new BubbleCounterAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance());
    }
}
