package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class D4CCounterAttack extends AbstractCounterAttack<D4CCounterAttack, D4CEntity> {
    private final CounterMissMove<D4CEntity> counterMiss = new CounterMissMove<>(10);

    public D4CCounterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull D4CEntity attacker, @NonNull LivingEntity user) {
        attacker.setMove(counterMiss, D4CEntity.State.COUNTER_MISS);
        StandEntity.stun(user, counterMiss.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull D4CEntity attacker, Entity countered, DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);
        var bl = counteredDamageSource.isOf(DamageTypes.MOB_PROJECTILE);
        var bl2 = counteredDamageSource.isOf(DamageTypes.MAGIC);

        if (countered == null || !attacker.hasUser() || bl || bl2)
            return;

        LivingEntity user = attacker.getUserOrThrow();
        Vec3d trueKnockback = countered.getPos().subtract(user.getPos()).normalize().multiply(1.5);
        countered.addVelocity(trueKnockback.x, 0.5, trueKnockback.z);
        countered.velocityModified = true;

        if (countered instanceof LivingEntity livingEntity) {
            livingEntity.damage(livingEntity.getWorld().getDamageSources().mobAttack(user), 10);
            StandEntity.stun(livingEntity, 20, 3);
            JUtils.cancelMoves(livingEntity);
        }

        attacker.getWorld().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 1f);
        attacker.playSound(JSoundRegistry.D4C_COUNTER, 1, 1);
    }

    @Override
    protected @NonNull D4CCounterAttack getThis() {
        return this;
    }

    @Override
    public @NonNull D4CCounterAttack copy() {
        return copyExtras(new D4CCounterAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
