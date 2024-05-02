package net.arna.jcraft.datagen;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;

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
        // sinner's soul
        ShapedRecipeJsonBuilder.create(JObjectRegistry.SINNERSSOUL)
                .pattern("SSS")
                .pattern("SFS")
                .pattern("SSS")
                .input('F', Items.FERMENTED_SPIDER_EYE)
                .input('S', Items.SOUL_SAND)
                .criterion("has_soul_sand", InventoryChangedCriterion.Conditions.items(Items.SOUL_SAND))
                .offerTo(exporter);
        // living arrow
        ShapelessRecipeJsonBuilder.create(JObjectRegistry.LIVINGARROW)
                .input(JObjectRegistry.STANDARROW)
                .input(JObjectRegistry.SINNERSSOUL)
                .criterion("has_arrow", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STANDARROW))
                .criterion("has_sinners_soul", InventoryChangedCriterion.Conditions.items(JObjectRegistry.SINNERSSOUL))
                .offerTo(exporter);
        // soul block
        ShapelessRecipeJsonBuilder.create(JObjectRegistry.SOUL_BLOCK)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .input(JObjectRegistry.SINNERSSOUL)
                .criterion("has_sinners_soul", InventoryChangedCriterion.Conditions.items(JObjectRegistry.SINNERSSOUL))
                .offerTo(exporter);
        // requiem ruby
        ShapedRecipeJsonBuilder.create(JObjectRegistry.REQUIEMRUBY)
                .pattern("RDR")
                .pattern("ENE")
                .pattern("RDR")
                .input('D', Items.DIAMOND_BLOCK)
                .input('E', Items.EMERALD_BLOCK)
                .input('N', Items.NETHER_STAR)
                .input('R', Items.REDSTONE_BLOCK)
                .criterion("has_nether_star", InventoryChangedCriterion.Conditions.items(Items.NETHER_STAR))
                .criterion("has_redstone_block", InventoryChangedCriterion.Conditions.items(Items.REDSTONE_BLOCK))
                .offerTo(exporter);
        // requiem arrow
        ShapelessRecipeJsonBuilder.create(JObjectRegistry.REQUIEMARROW)
                .input(JObjectRegistry.STANDARROW)
                .input(JObjectRegistry.REQUIEMRUBY)
                .input(Items.TIPPED_ARROW)
                .criterion("has_arrow", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STANDARROW))
                .criterion("has_ruby", InventoryChangedCriterion.Conditions.items(JObjectRegistry.REQUIEMRUBY))
                .offerTo(exporter);
        // coffin
        ShapedRecipeJsonBuilder.create(JObjectRegistry.COFFIN_BLOCK)
                .pattern("SSS")
                .pattern("SBS")
                .input('B', ItemTags.BEDS)
                .input('S', ItemTags.WOODEN_SLABS)
                .criterion("has_black_bed", InventoryChangedCriterion.Conditions.items(Items.BLACK_BED))
                .criterion("has_blue_bed", InventoryChangedCriterion.Conditions.items(Items.BLUE_BED))
                .criterion("has_brown_bed", InventoryChangedCriterion.Conditions.items(Items.BROWN_BED))
                .criterion("has_cyan_bed", InventoryChangedCriterion.Conditions.items(Items.CYAN_BED))
                .criterion("has_gray_bed", InventoryChangedCriterion.Conditions.items(Items.GRAY_BED))
                .criterion("has_green_bed", InventoryChangedCriterion.Conditions.items(Items.GREEN_BED))
                .criterion("has_light_blue_bed", InventoryChangedCriterion.Conditions.items(Items.LIGHT_BLUE_BED))
                .criterion("has_light_grey_bed", InventoryChangedCriterion.Conditions.items(Items.LIGHT_GRAY_BED))
                .criterion("has_lime_bed", InventoryChangedCriterion.Conditions.items(Items.LIME_BED))
                .criterion("has_magenta_bed", InventoryChangedCriterion.Conditions.items(Items.MAGENTA_BED))
                .criterion("has_orange_bed", InventoryChangedCriterion.Conditions.items(Items.ORANGE_BED))
                .criterion("has_pink_bed", InventoryChangedCriterion.Conditions.items(Items.PINK_BED))
                .criterion("has_purple_bed", InventoryChangedCriterion.Conditions.items(Items.PURPLE_BED))
                .criterion("has_red_bed", InventoryChangedCriterion.Conditions.items(Items.RED_BED))
                .criterion("has_white_bed", InventoryChangedCriterion.Conditions.items(Items.WHITE_BED))
                .criterion("has_yellow_bed", InventoryChangedCriterion.Conditions.items(Items.YELLOW_BED))
                .offerTo(exporter);
        // Kars' headwrap
        ShapedRecipeJsonBuilder.create(JObjectRegistry.KARSHEADWRAP)
                .pattern(" C ")
                .pattern("L L")
                .pattern(" B ")
                .input('B', Items.BLACK_DYE)
                .input('C', Items.LEATHER_HELMET)
                .input('L', Items.LEATHER)
                .criterion("has_leather_helmet", InventoryChangedCriterion.Conditions.items(Items.LEATHER_HELMET))
                .offerTo(exporter);
        // red hat
        ShapedRecipeJsonBuilder.create(JObjectRegistry.RED_HAT)
                .pattern(" R ")
                .pattern("LCL")
                .input('C', Items.LEATHER_HELMET)
                .input('L', Items.LEATHER)
                .input('R', Items.RED_DYE)
                .criterion("has_leather_helmet", InventoryChangedCriterion.Conditions.items(Items.LEATHER_HELMET))
                .offerTo(exporter);
        // blood bottle
        ShapedRecipeJsonBuilder.create(JObjectRegistry.BLOOD_BOTTLE)
                .pattern(" B ")
                .pattern(" G ")
                .pattern("GGG")
                .input('B', ItemTags.BUTTONS)
                .input('G', Items.GLASS)
                .criterion("has_glass", InventoryChangedCriterion.Conditions.items(Items.GLASS))
                .offerTo(exporter);
        // Jotaro's cap
        ShapedRecipeJsonBuilder.create(JObjectRegistry.JOTAROCAP)
                .pattern("BYB")
                .pattern("BHB")
                .input('B', Items.BLACK_DYE)
                .input('H', Items.NETHERITE_HELMET)
                .input('Y', Items.YELLOW_DYE)
                .criterion("has_netherite_helmet", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_HELMET))
                .offerTo(exporter);
        // Jotaro's jacket
        ShapedRecipeJsonBuilder.create(JObjectRegistry.JOTAROJACKET)
                .pattern("B B")
                .pattern("BCB")
                .pattern("BBB")
                .input('B', Items.BLACK_DYE)
                .input('C', Items.NETHERITE_CHESTPLATE)
                .criterion("has_netherite_chestplate", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_CHESTPLATE))
                .offerTo(exporter);
        // Jotaro's pants
        ShapedRecipeJsonBuilder.create(JObjectRegistry.JOTAROPANTS)
                .pattern("YYY")
                .pattern("BLB")
                .pattern("B B")
                .input('B', Items.BLACK_DYE)
                .input('L', Items.NETHERITE_LEGGINGS)
                .input('Y', Items.YELLOW_DYE)
                .criterion("has_netherite_leggings", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_LEGGINGS))
                .offerTo(exporter);
        // Jotaro's boots
        ShapedRecipeJsonBuilder.create(JObjectRegistry.JOTAROBOOTS)
                .pattern("BNB")
                .pattern("B B")
                .input('B', Items.BLACK_DYE)
                .input('N', Items.NETHERITE_BOOTS)
                .criterion("has_netherite_boots", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_BOOTS))
                .offerTo(exporter);
        // Dio's headband
        ShapedRecipeJsonBuilder.create(JObjectRegistry.DIOHEADBAND)
                .pattern("GHG")
                .input('G', Items.GREEN_DYE)
                .input('H', Items.NETHERITE_HELMET)
                .criterion("has_netherite_helmet", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_HELMET))
                .offerTo(exporter);
        // Dio's jacket
        ShapedRecipeJsonBuilder.create(JObjectRegistry.DIOJACKET)
                .pattern("Y Y")
                .pattern("YCY")
                .pattern("YBY")
                .input('B', Items.BLACK_DYE)
                .input('C', Items.NETHERITE_CHESTPLATE)
                .input('Y', Items.YELLOW_DYE)
                .criterion("has_netherite_chestplate", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_CHESTPLATE))
                .offerTo(exporter);
        // Dio's cape
        ShapedRecipeJsonBuilder.create(JObjectRegistry.DIOCAPE)
                .pattern("RLR")
                .pattern("LCL")
                .pattern("LLL")
                .input('C', Items.NETHERITE_CHESTPLATE)
                .input('L', Items.LEATHER)
                .input('R', Items.RED_DYE)
                .criterion("has_netherite_chestplate", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_CHESTPLATE))
                .offerTo(exporter);
        // Dio's pants
        ShapedRecipeJsonBuilder.create(JObjectRegistry.DIOPANTS)
                .pattern("GGG")
                .pattern("YLY")
                .pattern("Y Y")
                .input('G', Items.GREEN_DYE)
                .input('L', Items.NETHERITE_LEGGINGS)
                .input('Y', Items.YELLOW_DYE)
                .criterion("has_netherite_leggings", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_LEGGINGS))
                .offerTo(exporter);
        // Dio's boots
        ShapedRecipeJsonBuilder.create(JObjectRegistry.DIOBOOTS)
                .pattern("YBY")
                .pattern("Y Y")
                .input('B', Items.NETHERITE_BOOTS)
                .input('Y', Items.YELLOW_DYE)
                .criterion("has_netherite_boots", InventoryChangedCriterion.Conditions.items(Items.NETHERITE_BOOTS))
                .offerTo(exporter);
    }
}
