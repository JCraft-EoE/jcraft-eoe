package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class TossMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<TossMove<A>, A> {

    @Getter
    private final float velocityMultiplier;

    @Getter
    private final float spreadMultiplier;

    public TossMove(int cooldown, int windup, int duration, float moveDistance) {
        this(cooldown, windup, duration, moveDistance, 1 / 40f);
    }

    public TossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier) {
        this(cooldown, windup, duration, moveDistance, velocityMultiplier, 1f);
    }

    /**
     *
     * @param cooldown
     * @param windup
     * @param duration
     * @param moveDistance
     * @param velocityMultiplier what to multiply the charge time with to get the velocity, defaults to <code>1/40f</code>.
     * @param spreadMultiplier what to multiply the inaccurancy with, defaults to <code>1f</code>.
     */
    public TossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier, float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance);
        this.velocityMultiplier = velocityMultiplier;
        this.spreadMultiplier = spreadMultiplier;
    }

    @Override
    public @NonNull MoveType<TossMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        if (attacker instanceof StandEntity<?,?> stand && !stand.level().isClientSide()) {
            final ItemStack projectile = stand.getItemInHand(InteractionHand.MAIN_HAND);
            JUtils.tossItem(stand, stand.level(), projectile, getChargeTime() * velocityMultiplier, spreadMultiplier, true);
        }
        return Set.of();
    }

    @Override
    protected @NonNull TossMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TossMove<A> copy() {
        return copyExtras(new TossMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractMove.Type<TossMove<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<TossMove<?>, Float> velocityMultiplier() {
            return Codec.FLOAT.fieldOf("velocityMultiplier").forGetter(TossMove::getVelocityMultiplier);
        }

        protected RecordCodecBuilder<TossMove<?>, Float> spreadMultiplier() {
            return Codec.FLOAT.fieldOf("spreadMultiplier").forGetter(TossMove::getSpreadMultiplier);
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TossMove<?>>, TossMove<?>> buildCodec(RecordCodecBuilder.Instance<TossMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, TossMove::new);
        }
    }
}
