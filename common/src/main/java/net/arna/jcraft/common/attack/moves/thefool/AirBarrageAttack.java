package net.arna.jcraft.common.attack.moves.thefool;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.minecraft.world.entity.LivingEntity;

public final class AirBarrageAttack<A extends IAttacker<? extends A, ?>> extends AbstractBarrageAttack<AirBarrageAttack<A>, A> {
    public AirBarrageAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                            final float hitboxSize, final float knockback, final float offset, final int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public @NonNull MoveType<AirBarrageAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void activeTick(final A attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);

        if (!attacker.hasUser()) {
            return;
        }

        final LivingEntity user = attacker.getUserOrThrow();
        user.setDeltaMovement(user.getDeltaMovement().scale(0.5).add(0, 0.01, 0));
        user.hurtMarked = true;
    }

    @Override
    protected @NonNull AirBarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull AirBarrageAttack<A> copy() {
        return copyExtras(new AirBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<AirBarrageAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<AirBarrageAttack<?>>, AirBarrageAttack<?>> buildCodec(RecordCodecBuilder.Instance<AirBarrageAttack<?>> instance) {
            return barrageDefault(instance, AirBarrageAttack::new);
        }
    }
}
