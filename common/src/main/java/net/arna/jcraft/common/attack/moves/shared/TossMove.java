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
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class TossMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<TossMove<A>, A> {

    @Getter
    private final float velocityMultiplier;

    public TossMove(int cooldown, int windup, int duration, float moveDistance) {
        this(cooldown, windup, duration, moveDistance, 1 / 40f);
    }

    /**
     *
     * @param cooldown
     * @param windup
     * @param duration
     * @param moveDistance
     * @param velocityMultiplier what to multiply the charge time with to get the velocity, defaults to <code>1/40f</code>.
     */
    public TossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance);
        this.velocityMultiplier = velocityMultiplier;
    }

    @Override
    public @NonNull MoveType<TossMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        if (attacker instanceof StandEntity<?,?> stand && !stand.level().isClientSide()) {
            final ItemStack projectile = stand.getItemInHand(InteractionHand.MAIN_HAND);
            final Vec3 lookAngle = user.getLookAngle();
            final Vec3 throwPos = new Vec3(
                    user.getX() + lookAngle.x * 0.3,
                    user.getY() + user.getBbHeight() * 0.5,
                    user.getZ() + lookAngle.z * 0.3
            );
            JUtils.tossItem(stand, stand.level(), projectile, getChargeTime() * velocityMultiplier, true, throwPos);
        }
        return Set.of();
    }

    @Override
    protected @NonNull TossMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TossMove<A> copy() {
        return copyExtras(new TossMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier()));
    }

    public static class Type extends AbstractMove.Type<TossMove<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<TossMove<?>, Float> velocityMultiplier() {
            return Codec.FLOAT.fieldOf("velocityMultiplier").forGetter(TossMove::getVelocityMultiplier);
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TossMove<?>>, TossMove<?>> buildCodec(RecordCodecBuilder.Instance<TossMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier()).apply(instance, TossMove::new);
        }
    }
}
