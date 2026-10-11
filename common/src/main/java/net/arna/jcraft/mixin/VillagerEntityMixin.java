package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.item.BloodBottleItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Villager.class)
public abstract class VillagerEntityMixin {

    @WrapMethod(method = "mobInteract")
    private InteractionResult interactMob(final Player player, final InteractionHand hand, final Operation<InteractionResult> original) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BloodBottleItem) {
            return InteractionResult.PASS;
        }
        return original.call(player, hand);
    }

}
