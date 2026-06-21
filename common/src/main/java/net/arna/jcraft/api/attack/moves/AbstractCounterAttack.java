package net.arna.jcraft.api.attack.moves;

import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
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
        if (living.getFirstPassenger() instanceof final StandEntity<?, ?> stand) {
            final AbstractMove<?, ?> attack = stand.getCurrentMove();

            if (attack == null || !attack.isCounter() || stand.getMoveStun() >= (attack.getDuration() - attack.getWindup()))
                return;

            final var causingEntity = source.getEntity();

            if (causingEntity != null) {
                //noinspection unchecked,rawtypes
                ((AbstractCounterAttack) attack).counter(stand, causingEntity, source);
                living.removeEffect(JStatusRegistry.DAZED.get());
                info.cancel();
            }
        }
    }
}
