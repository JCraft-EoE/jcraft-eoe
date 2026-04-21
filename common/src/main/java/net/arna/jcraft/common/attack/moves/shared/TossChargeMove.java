package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TossChargeMove<A extends IAttacker<A, ?>> extends AbstractHoldableMove<TossChargeMove<A>, A> {

    public TossChargeMove(int cooldown, int windup, int duration, float moveDistance, int minimumCharge) {
        super(cooldown, windup, duration, moveDistance, minimumCharge);
    }

    @Override
    public @NonNull MoveType<TossChargeMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        if (attacker instanceof StandEntity<?,?> stand && !stand.level().isClientSide()) {
            final LivingEntity user = stand.getUser();
            if (user == null) {
                return;
            }
            ItemStack projectileSource = user.getItemInHand(InteractionHand.MAIN_HAND);
            if (projectileSource.isEmpty()) {
                projectileSource = user.getItemInHand(InteractionHand.OFF_HAND);
            }
            if (!projectileSource.isEmpty()) {
                final ItemStack oldProjectile = stand.getItemInHand(InteractionHand.MAIN_HAND);
                if (oldProjectile.isEmpty()) {
                    stand.setItemInHand(InteractionHand.MAIN_HAND, projectileSource.copyWithCount(1));
                    if (!(user instanceof Player player) || !player.isCreative()) {
                        projectileSource.shrink(1);
                    }
                }
            }
        }
    }

    @Override
    protected @NonNull TossChargeMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TossChargeMove<A> copy() {
        return copyExtras(new TossChargeMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getMinimumCharge()));
    }

    public static class Type extends AbstractHoldableMove.Type<TossChargeMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TossChargeMove<?>>, TossChargeMove<?>> buildCodec(RecordCodecBuilder.Instance<TossChargeMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), minimumCharge()).apply(instance, TossChargeMove::new);
        }
    }
}
