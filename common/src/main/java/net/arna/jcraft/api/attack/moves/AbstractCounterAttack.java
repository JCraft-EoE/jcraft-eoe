package net.arna.jcraft.api.attack.moves;

import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

public abstract class AbstractCounterAttack<T extends AbstractCounterAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractMove<T, A> {
    protected AbstractCounterAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        counter = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        if (attacker instanceof StandEntity<?, ?> stand && stand.isFree()) {
            stand.setFree(false);
        }
        return Set.of();
    }

    /**
     * Called when this counter-attack missed.
     * Typically, sets the attack to a counter miss attack and stuns the user.
     *
     * @param attacker The stand that missed
     * @param user     The stand's user
     */
    public abstract void whiff(final @NonNull A attacker, final @NonNull LivingEntity user);

    public boolean canCounter(DamageSource source, float amount) {
        if (amount < 1.0f && !source.is(JDamageSources.STAND)) return false;

        return !source.is(DamageTypeTags.IS_PROJECTILE)
                && !source.is(JDamageSources.PHPOISON)
                && !source.is(JDamageSources.WHITE_SNAKE_POISON);
    }

    /**
     * Called when this counter-attack hit.
     * Typically, it resets the user's attack, but can also somehow reward them for it.
     *
     * @param attacker              The stand that hit
     * @param countered             The entity whose attack was countered
     * @param counteredDamageSource The damage source the countered entity was using
     */
    public void counter(final @NonNull A attacker, final Entity countered, final DamageSource counteredDamageSource) {
        attacker.setMoveStun(0);
        attacker.setCurrentMove(null);
    }

    public static void handleCounter(LivingEntity living, DamageSource source, float amount, CallbackInfo info) {
        final StandEntity<?, ?> stand = JUtils.getStand(living);

        if (stand == null) return;

        final AbstractMove<?, ?> attack = stand.getCurrentMove();

        if (attack == null || !attack.isCounter() || stand.getMoveStun() >= (attack.getDuration() - attack.getWindup()))
            return;

        //noinspection rawtypes
        var counter = (AbstractCounterAttack)attack;

        if ( !counter.canCounter(source, amount) ) return;

        final var causingEntity = source.getEntity();

        if (causingEntity != null) {
            //noinspection unchecked
            counter.counter(stand, causingEntity, source);
            living.removeEffect(JStatusRegistry.DAZED.get());
            info.cancel();
        }
    }
}
