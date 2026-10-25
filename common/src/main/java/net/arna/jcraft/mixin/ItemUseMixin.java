package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({Item.class, BowItem.class, CrossbowItem.class, TridentItem.class})
public abstract class ItemUseMixin {

    @WrapMethod(method = "use")
    private InteractionResultHolder<ItemStack> jcraft$onUse(final Level level, final Player player, final InteractionHand usedHand, final Operation<InteractionResultHolder<ItemStack>> original) {
        if (player.hasEffect(JStatusRegistry.DAZED.get())) {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }
        return original.call(level, player, usedHand);
    }

    @WrapMethod(method = "releaseUsing") // Inability to use items while stunned
    private void jcraft$onStoppedUsing(final ItemStack stack, final Level level, final LivingEntity livingEntity, final int timeCharged, final Operation<Void> original) {
        if (!livingEntity.hasEffect(JStatusRegistry.DAZED.get())) {
            original.call(stack, level, livingEntity, timeCharged);
        }
    }
}
