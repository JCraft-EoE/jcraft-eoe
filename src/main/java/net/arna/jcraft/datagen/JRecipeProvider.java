package net.arna.jcraft.datagen;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

import java.util.function.Consumer;

public class JRecipeProvider extends FabricRecipeProvider {

    public JRecipeProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        // stellar iron ingot from smelting
        CookingRecipeJsonBuilder.createSmelting(
                        Ingredient.ofItems(JObjectRegistry.METEORITE_IRON_ORE_BLOCK),
                        JObjectRegistry.STELLAR_IRON_INGOT,
                        2f,
                        200)
                .criterion("has_ore", InventoryChangedCriterion.Conditions.items(JObjectRegistry.METEORITE_IRON_ORE_BLOCK))
                .offerTo(exporter, JCraft.MOD_ID + ":stellar_iron_ingot_from_smelting");
        // stellar iron ingot from blasting
        CookingRecipeJsonBuilder.createBlasting(
                        Ingredient.ofItems(JObjectRegistry.METEORITE_IRON_ORE_BLOCK),
                        JObjectRegistry.STELLAR_IRON_INGOT,
                        2f,
                        100)
                .criterion("has_ore", InventoryChangedCriterion.Conditions.items(JObjectRegistry.METEORITE_IRON_ORE_BLOCK))
                .offerTo(exporter, JCraft.MOD_ID + ":stellar_iron_ingot_from_blasting");
        // stand arrowhead
        ShapedRecipeJsonBuilder.create(JObjectRegistry.STAND_ARROWHEAD, 3)
                .pattern("NGI")
                .pattern("GIG")
                .pattern(" GN")
                .input('G', Items.GOLD_INGOT)
                .input('I', JObjectRegistry.STELLAR_IRON_INGOT)
                .input('N', Items.GOLD_NUGGET)
                .criterion("has_ingot", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STELLAR_IRON_INGOT))
                .offerTo(exporter);
        // stand arrow
        ShapedRecipeJsonBuilder.create(JObjectRegistry.STANDARROW)
                .pattern("  A")
                .pattern(" S ")
                .pattern("F  ")
                .input('A', JObjectRegistry.STAND_ARROWHEAD)
                .input('F', Items.FEATHER)
                .input('S', Items.STICK)
                .criterion("has_arrowhead", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STAND_ARROWHEAD))
                .offerTo(exporter);
        // stand disk
        ShapedRecipeJsonBuilder.create(JObjectRegistry.STAND_DISC)
                .pattern("FFF")
                .pattern("FAF")
                .pattern("FFF")
                .input('A', JObjectRegistry.STANDARROW)
                .input('F', Items.DISC_FRAGMENT_5)
                .criterion("has_arrow", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STANDARROW))
                .offerTo(exporter);
    }
}
