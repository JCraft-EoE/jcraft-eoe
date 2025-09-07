package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.enchantments.CinderellasKissEnchantment;
import net.arna.jcraft.common.item.StandDiscItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;

    private AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isDamageableItem()Z",
                    ordinal = 0
            )
    )
    private void injectStandSkinRecipe(CallbackInfo ci, @Local(ordinal = 1) ItemStack result) {
        ItemStack item1 = inputSlots.getItem(0); // Stand disc
        ItemStack item2 = inputSlots.getItem(1); // Cinderella mask

        if (!isValidStandSkinRecipe(item1, item2)) return;

        // Set the result to the disc with the skin.
        int level = CinderellasKissEnchantment.getCKLevel(item2);
        StandDiscItem.setSkin(result, level);
        cost.set(5);
    }

    @ModifyVariable(
            method = "createResult",
            at = @At(
                    value = "LOAD",
                    ordinal = 0
            ),
            index = 8
    )
    private boolean modifyReturnCondition1(boolean isValid) {
        // Modifies the first return condition. Allows for stand skins to be crafted.
        return isValid || isValidStandSkinRecipe(inputSlots.getItem(0), inputSlots.getItem(1));
    }

    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantments(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Map;",
                    ordinal = 1
            )
    )
    private Map<Enchantment, Integer> modifyMaskEnchantments(Map<Enchantment, Integer> original, @Local(ordinal = 1) ItemStack stack) {
        // Pretend masks have no enchantments.
        return stack.is(JItemRegistry.CINDERELLA_MASK.get()) ? Map.of() : original;
    }

    @ModifyVariable(
            method = "createResult",
            at = @At("LOAD"),
            index = 10
    )
    private boolean modifyReturnCondition2(boolean isNotValid) {
        // Modifies the second return condition. Normally checks for enchantment compatibility.
        return isNotValid && !isValidStandSkinRecipe(inputSlots.getItem(0), inputSlots.getItem(1));
    }

    @ModifyArg(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V",
                    ordinal = 5
            )
    )
    private int modifyCost(int cost, @Local(ordinal = 0) LocalIntRef i) {
        // For some reason, even though we set i here, the code still uses the original value of i,
        // which is always going to be 0. So we set it to 5 to have it not exit, but also return the correct cost.
        // If this is a skin recipe, the base cost is 5.
        if (inputSlots.getItem(1).is(JItemRegistry.CINDERELLA_MASK.get())) {
            i.set(5);
            return cost + 5;
        }

        return cost;
    }

    @ModifyExpressionValue(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 3
            )
    )
    private boolean modifyRepairCostCondition(boolean b) {
        // If this is a skin recipe, we don't want to apply a repair cost or enchantments.
        return !inputSlots.getItem(1).is(JItemRegistry.CINDERELLA_MASK.get()) && b;
    }

    private static @Unique boolean isValidStandSkinRecipe(ItemStack item1, ItemStack item2) {
        // Check whether the items are stand disc and cinderella mask
        if (item1.getItem() != JItemRegistry.STAND_DISC.get() || item2.getItem() != JItemRegistry.CINDERELLA_MASK.get())
            return false;

        // If the disc is empty, return.
        StandType standType = StandDiscItem.getStandType(item1);
        if (standType == null)
            return false;

        // The CK level must be at most equal to the amount of skins the stand has
        // and must not be the same as the disc's current skin.
        int level = CinderellasKissEnchantment.getCKLevel(item2);
        return level <= standType.getData().getInfo().getSkinCount() - 1 && level != StandDiscItem.getSkin(item1);
    }
}
