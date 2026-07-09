package net.arna.jcraft.common.recipe;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JRecipeRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.item.StandDiscItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;

public class CrazyDiamondRecipe extends SingleItemRecipe {

    private static final ItemStack TOAST = StandDiscItem.createDiscStack(JStandTypeRegistry.CRAZY_DIAMOND.get(), 0);

    public CrazyDiamondRecipe(final @NonNull ResourceLocation id, final String group, final @NonNull Ingredient ingredient, final @NonNull ItemStack result) {
        super(JRecipeRegistry.CD_TYPE.get(), JRecipeRegistry.CD_SERIALIZER.get(), id, group, ingredient, result);
    }

    @Override
    public boolean matches(final @NonNull Container container, final @NonNull Level level) {
        if (level.isClientSide()) {
            return false;
        }
        return ingredient.test(container.getItem(0));
    }

    @Override
    public @NonNull ItemStack getToastSymbol() {
        return TOAST;
    }

    public static class Serializer extends SingleItemRecipe.Serializer<CrazyDiamondRecipe> {
        public Serializer() {
            super(CrazyDiamondRecipe::new);
        }
    }
}
