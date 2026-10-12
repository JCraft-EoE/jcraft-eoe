package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.spec.SpinSpec;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SpinChargeMove<A extends IAttacker<A, ?>> extends AbstractHoldableMove<SpinChargeMove<A>, A> {

    public SpinChargeMove(int cooldown, int windup, int duration, float moveDistance, int minimumCharge) {
        super(cooldown, windup, duration, moveDistance, minimumCharge);
    }

    @Override
    public @NonNull MoveType<SpinChargeMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        if (!(attacker instanceof SpinSpec spinSpec) || spinSpec.getBaseEntity() == null || spinSpec.getBaseEntity().level().isClientSide()) {
            return;
        }
        final LivingEntity baseEntity = spinSpec.getBaseEntity();

        InteractionHand itemHand = InteractionHand.MAIN_HAND;
        ItemStack spinningItem = baseEntity.getItemInHand(itemHand);
        if (spinningItem.isEmpty() || spinningItem.is(JTagRegistry.UNTHROWABLE) || spinningItem.getCount() != 1) {
            itemHand = InteractionHand.OFF_HAND;
            spinningItem = baseEntity.getItemInHand(itemHand);
        }
        final CommonSpinComponent spin = JComponentPlatformUtils.getSpin(attacker.getBaseEntity());
        if (spinningItem.isEmpty() || spinningItem.is(JTagRegistry.UNTHROWABLE) || spinningItem.getCount() != 1) {
            spin.cancelSpinning();
        }
        else {
            if (attacker.getBaseEntity() instanceof final Player player) {
                spin.setSlot(itemHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : (byte)player.getInventory().selected);
            }
            else {
                spin.setSlot(itemHand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : (byte)0);
            }
        }
    }

    @Override
    public boolean conditionsMet(A attacker) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        ItemStack spinningItem = baseEntity.getItemInHand(InteractionHand.MAIN_HAND);
        if (spinningItem.isEmpty() || spinningItem.is(JTagRegistry.UNTHROWABLE) || spinningItem.getCount() != 1) {
            spinningItem = baseEntity.getItemInHand(InteractionHand.OFF_HAND);
        }
        return !spinningItem.isEmpty() && !spinningItem.is(JTagRegistry.UNTHROWABLE) && spinningItem.getCount() == 1;
    }

    @Override
    public void onCancel(final A attacker) {
        if (attacker != null && attacker.getBaseEntity() != null) {
            JComponentPlatformUtils.getSpin(attacker.getBaseEntity()).cancelSpinning();
        }
    }

    @Override
    protected @NonNull SpinChargeMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SpinChargeMove<A> copy() {
        return copyExtras(new SpinChargeMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getMinimumCharge()));
    }

    public static class Type extends AbstractHoldableMove.Type<SpinChargeMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SpinChargeMove<?>>, SpinChargeMove<?>> buildCodec(RecordCodecBuilder.Instance<SpinChargeMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), minimumCharge()).apply(instance, SpinChargeMove::new);
        }
    }

}
