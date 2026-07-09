package net.arna.jcraft.datagen.builder;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JRecipeRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CrazyDiamondRecipeBuilder implements RecipeBuilder {

    private Ingredient ingredient;
    private final ItemStack result;
    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();

    public CrazyDiamondRecipeBuilder(final ItemLike result) {
        this(new ItemStack(result));
    }

    public CrazyDiamondRecipeBuilder(final ItemStack result) {
        this.result = result;
    }

    @Override
    public @NotNull CrazyDiamondRecipeBuilder unlockedBy(final @NonNull String string, final @NonNull CriterionTriggerInstance criterion) {
        criteria.put(string, criterion);
        return this;
    }

    @Override
    public @NotNull CrazyDiamondRecipeBuilder group(final @Nullable String string) {
        // group is ignored for now
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result.getItem();
    }

    public CrazyDiamondRecipeBuilder requires(final @NotNull ItemLike ingredient) {
        return requires(Ingredient.of(ingredient));
    }

    public CrazyDiamondRecipeBuilder requires(final @NotNull Ingredient ingredient) {
        if (this.ingredient != null) {
            throw new IllegalStateException("Ingredient already defined!");
        }
        this.ingredient = ingredient;
        return this;
    }

    @Override
    public void save(final @NonNull Consumer<FinishedRecipe> finishedRecipeConsumer, final @NonNull ResourceLocation resourceLocation) {
        ensureValid(resourceLocation);
        final Advancement.Builder builder = Advancement.Builder.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(RequirementsStrategy.OR);
        criteria.forEach(builder::addCriterion);
        // final CrazyDiamondRecipe cdRecipe = new CrazyDiamondRecipe(resourceLocation, null, ingredient, result);
        finishedRecipeConsumer.accept(
                new SingleItemRecipeBuilder.Result(
                        resourceLocation,
                        JRecipeRegistry.CD_SERIALIZER.get(),
                        "",
                        this.ingredient,
                        this.result.getItem(),
                        1,
                        builder,
                        resourceLocation.withPrefix("recipes/crazy_diamond/")
                )
        );
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (ingredient == null) {
            throw new IllegalStateException("No ingredient specified for recipe " + resourceLocation + "!");
        }
    }
}
