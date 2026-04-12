package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.recipe.CrazyDiamondRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface JRecipeRegistry {

    DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, Registries.RECIPE_SERIALIZER);

    RegistrySupplier<RecipeSerializer<CrazyDiamondRecipe>> CD_SERIALIZER = SERIALIZER_REGISTRY.register(JCraft.id("crazy_diamond"), CrazyDiamondRecipe.Serializer::new);

    DeferredRegister<RecipeType<?>> TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, Registries.RECIPE_TYPE);

    RegistrySupplier<RecipeType<CrazyDiamondRecipe>> CD_TYPE = registerType(JCraft.id("crazy_diamond"));

    static <T extends Recipe<?>> RegistrySupplier<RecipeType<T>> registerType(final @NotNull ResourceLocation id) {
        return TYPE_REGISTRY.register(id, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return id.getPath();
            }
        });
    }

    static void register() {
        SERIALIZER_REGISTRY.register();
        TYPE_REGISTRY.register();
    }
}
