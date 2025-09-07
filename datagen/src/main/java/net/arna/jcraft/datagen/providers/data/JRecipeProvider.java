package net.arna.jcraft.datagen.providers.data;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JBlockRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

public class JRecipeProvider extends FabricRecipeProvider {

    public JRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        // polished meteorite block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JItemRegistry.POLISHED_METEORITE_BLOCK.get(), 4)
                .pattern("BB")
                .pattern("BB")
                .define('B', JItemRegistry.METEORITE_BLOCK.get())
                .unlockedBy("has_block", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.POLISHED_METEORITE_BLOCK.get()))
                .save(exporter);
        // stellar iron ingot from smelting
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get()),
                        RecipeCategory.MISC,
                        JItemRegistry.STELLAR_IRON_INGOT.get(),
                        2f,
                        200)
                .unlockedBy("has_ore", InventoryChangeTrigger.TriggerInstance.hasItems(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get()))
                .save(exporter, JCraft.id("stellar_iron_ingot_from_smelting"));
        // stellar iron ingot from blasting
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get()),
                        RecipeCategory.MISC,
                        JItemRegistry.STELLAR_IRON_INGOT.get(),
                        2f,
                        100)
                .unlockedBy("has_ore", InventoryChangeTrigger.TriggerInstance.hasItems(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get()))
                .save(exporter, JCraft.id("stellar_iron_ingot_from_blasting"));
        // stellar iron ingot from block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, JItemRegistry.STELLAR_IRON_INGOT.get(), 9)
                .requires(JBlockRegistry.STELLAR_IRON_BLOCK.get())
                .unlockedBy("has_block", InventoryChangeTrigger.TriggerInstance.hasItems(JBlockRegistry.STELLAR_IRON_BLOCK.get()))
                .save(exporter, JCraft.id("stellar_iron_ingot_from_block"));
        // disc from smelting
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Items.LIGHT_BLUE_STAINED_GLASS_PANE),
                        RecipeCategory.MISC,
                        JItemRegistry.DISC.get(),
                        0.3f,
                        200)
                .unlockedBy("has_glass_pane", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LIGHT_BLUE_STAINED_GLASS_PANE))
                .save(exporter, JCraft.id("disc"));
        // stellar iron block from ingot
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, JBlockRegistry.STELLAR_IRON_BLOCK.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .requires(JItemRegistry.STELLAR_IRON_INGOT.get())
                .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STELLAR_IRON_INGOT.get()))
                .save(exporter);
        // cinderella green from smelting
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JItemRegistry.CINDERELLA_GREEN_BLOCK.get())
                .pattern(" B ")
                .pattern("YLY")
                .pattern(" B ")
                .define('B', Items.BAMBOO)
                .define('L', Items.LIME_TERRACOTTA)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_bamboo", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BAMBOO))
                .save(exporter);
        // stand arrowhead
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.STAND_ARROWHEAD.get(), 3)
                .pattern("NGI")
                .pattern("GIG")
                .pattern(" GN")
                .define('G', Items.GOLD_INGOT)
                .define('I', JItemRegistry.STELLAR_IRON_INGOT.get())
                .define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STELLAR_IRON_INGOT.get()))
                .save(exporter);
        // stand arrow
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.STAND_ARROW.get())
                .pattern("  A")
                .pattern(" S ")
                .pattern("F  ")
                .define('A', JItemRegistry.STAND_ARROWHEAD.get())
                .define('F', Items.FEATHER)
                .define('S', Items.STICK)
                .unlockedBy("has_arrowhead", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STAND_ARROWHEAD.get()))
                .save(exporter);
        // stand disk
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.STAND_DISC.get())
                .pattern("BBB")
                .pattern("BAB")
                .pattern("BDB")
                .define('A', JItemRegistry.STAND_ARROWHEAD.get())
                .define('D', JItemRegistry.DISC.get())
                .define('B', JItemRegistry.POLISHED_METEORITE_BLOCK.get())
                .unlockedBy("has_arrow", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STAND_ARROW.get()))
                .unlockedBy("has_disc", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DISC.get()))
                .save(exporter);
        // spec disk
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.SPEC_DISC.get())
                .pattern("DA")
                .define('A', Items.EXPERIENCE_BOTTLE)
                .define('D', JItemRegistry.DISC.get())
                .unlockedBy("has_disc", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DISC.get()))
                .save(exporter);
        // sinner's soul
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.SINNERS_SOUL.get())
                .pattern("SSS")
                .pattern("SFS")
                .pattern("SSS")
                .define('F', Items.FERMENTED_SPIDER_EYE)
                .define('S', Items.SOUL_SAND)
                .unlockedBy("has_soul_sand", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SOUL_SAND))
                .save(exporter);
        // sinner's soul from soul block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, JItemRegistry.SINNERS_SOUL.get(), 9)
                .requires(JBlockRegistry.SOUL_BLOCK.get())
                .unlockedBy("has_soul_block", InventoryChangeTrigger.TriggerInstance.hasItems(JBlockRegistry.SOUL_BLOCK.get()))
                .save(exporter, JCraft.id("sinners_soul_from_soul_block"));
        // living arrow
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, JItemRegistry.LIVING_ARROW.get())
                .requires(JItemRegistry.STAND_ARROW.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .unlockedBy("has_arrow", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STAND_ARROW.get()))
                .unlockedBy("has_sinners_soul", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.SINNERS_SOUL.get()))
                .save(exporter);
        // soul block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, JBlockRegistry.SOUL_BLOCK.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .requires(JItemRegistry.SINNERS_SOUL.get())
                .unlockedBy("has_sinners_soul", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.SINNERS_SOUL.get()))
                .save(exporter);
        // requiem ruby
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.REQUIEM_RUBY.get())
                .pattern("RDR")
                .pattern("ENE")
                .pattern("RDR")
                .define('N', Items.NETHER_STAR)
                .define('E', Items.EMERALD_BLOCK)
                .define('R', Items.REDSTONE_BLOCK)
                .define('D', Items.DIAMOND_BLOCK)
                .unlockedBy("has_nether_star", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHER_STAR))
                .unlockedBy("has_redstone_block", InventoryChangeTrigger.TriggerInstance.hasItems(Items.REDSTONE_BLOCK))
                .save(exporter);
        // requiem arrow
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, JItemRegistry.REQUIEM_ARROW.get())
                .requires(JItemRegistry.STAND_ARROW.get())
                .requires(JItemRegistry.REQUIEM_RUBY.get())
                .requires(Items.TIPPED_ARROW)
                .unlockedBy("has_arrow", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STAND_ARROW.get()))
                .unlockedBy("has_ruby", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.REQUIEM_RUBY.get()))
                .save(exporter);
        // coffin
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.COFFIN_BLOCK.get())
                .pattern("SSS")
                .pattern("SBS")
                .define('B', ItemTags.BEDS)
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_black_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BLACK_BED))
                .unlockedBy("has_blue_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BLUE_BED))
                .unlockedBy("has_brown_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BROWN_BED))
                .unlockedBy("has_cyan_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CYAN_BED))
                .unlockedBy("has_gray_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GRAY_BED))
                .unlockedBy("has_green_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GREEN_BED))
                .unlockedBy("has_light_blue_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LIGHT_BLUE_BED))
                .unlockedBy("has_light_grey_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LIGHT_GRAY_BED))
                .unlockedBy("has_lime_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LIME_BED))
                .unlockedBy("has_magenta_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.MAGENTA_BED))
                .unlockedBy("has_orange_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ORANGE_BED))
                .unlockedBy("has_pink_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PINK_BED))
                .unlockedBy("has_purple_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PURPLE_BED))
                .unlockedBy("has_red_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.RED_BED))
                .unlockedBy("has_white_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.WHITE_BED))
                .unlockedBy("has_yellow_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.YELLOW_BED))
                .save(exporter);
        // Strazo's Poncho
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.STRAIZO_PONCHO.get())
                .pattern("BBB")
                .pattern("BCB")
                .pattern(" B ")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Kars' headwrap
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KARS_HEADWRAP.get())
                .pattern(" C ")
                .pattern("L L")
                .pattern(" B ")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.LEATHER_HELMET)
                .define('L', Items.LEATHER)
                .unlockedBy("has_leather_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER_HELMET))
                .save(exporter);
        // red hat
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RED_HAT.get())
                .pattern(" R ")
                .pattern("LCL")
                .define('C', Items.LEATHER_HELMET)
                .define('L', Items.LEATHER)
                .define('R', Items.RED_DYE)
                .unlockedBy("has_leather_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER_HELMET))
                .save(exporter);
        // blood bottle
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.BLOOD_BOTTLE.get())
                .pattern(" B ")
                .pattern(" G ")
                .pattern("GGG")
                .define('B', ItemTags.BUTTONS)
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLASS))
                .save(exporter);
        // Dio's P1 wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_P1_WIG.get())
                .pattern(" W ")
                .pattern("WHW")
                .define('H', Items.NETHERITE_HELMET)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Dio's P1 jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_P1_JACKET.get())
                .pattern("RPR")
                .pattern("RCR")
                .pattern("WBW")
                .define('B', Items.BROWN_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('P', Items.PURPLE_DYE)
                .define('R', Items.RED_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Dio's P1 pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_P1_PANTS.get())
                .pattern("RWR")
                .pattern("RLR")
                .pattern("R R")
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('R', Items.RED_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Dio's P1 boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_P1_BOOTS.get())
                .pattern("PNP")
                .pattern("B B")
                .define('B', Items.BLACK_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Jotaro's cap
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_CAP.get())
                .pattern("BYB")
                .pattern("BHB")
                .define('B', Items.BLACK_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Jotaro's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_JACKET.get())
                .pattern("B B")
                .pattern("BCB")
                .pattern("BBB")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Jotaro's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_PANTS.get())
                .pattern("YYY")
                .pattern("BLB")
                .pattern("B B")
                .define('B', Items.BLACK_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Jotaro's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_BOOTS.get())
                .pattern("BNB")
                .pattern("B B")
                .define('B', Items.BLACK_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Kakyoin's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KAKYOIN_WIG.get())
                .pattern("WRW")
                .pattern("RHR")
                .define('H', Items.NETHERITE_HELMET)
                .define('R', Items.RED_DYE)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Kakyoin's coat
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KAKYOIN_COAT.get())
                .pattern("GYG")
                .pattern("GCG")
                .pattern("GGG")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.GREEN_DYE)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Kakyoin's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KAKYOIN_PANTS.get())
                .pattern("GGG")
                .pattern("GLG")
                .pattern("G G")
                .define('G', Items.GREEN_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Kakyoin's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KAKYOIN_BOOTS.get())
                .pattern("BNB")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Dio's headband
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_HEADBAND.get())
                .pattern("GHG")
                .define('G', Items.GREEN_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Dio's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_JACKET.get())
                .pattern("Y Y")
                .pattern("YCY")
                .pattern("YBY")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Dio's cape
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_CAPE.get())
                .pattern("RLR")
                .pattern("LCL")
                .pattern("LLL")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('L', Items.LEATHER)
                .define('R', Items.RED_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Dio's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_PANTS.get())
                .pattern("GGG")
                .pattern("YLY")
                .pattern("Y Y")
                .define('G', Items.GREEN_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Dio's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIO_BOOTS.get())
                .pattern("YBY")
                .pattern("Y Y")
                .define('B', Items.NETHERITE_BOOTS)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Heaven Attained Dio's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.HEAVEN_ATTAINED_WIG.get())
                .pattern("SSS")
                .pattern("SHS")
                .pattern("S S")
                .define('H', Items.NETHERITE_HELMET)
                .define('S', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Heaven Attained Dio's shirt
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.HEAVEN_ATTAINED_SHIRT.get())
                .pattern("WWW")
                .pattern("GCG")
                .pattern("X X")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.GOLD_INGOT)
                .define('X', Items.WHITE_CARPET)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Heaven Attained Dio's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.HEAVEN_ATTAINED_PANTS.get())
                .pattern("WWW")
                .pattern("GLG")
                .pattern("W W")
                .define('G', Items.GOLD_INGOT)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Heaven Attained Dio's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.HEAVEN_ATTAINED_BOOTS.get())
                .pattern("GBG")
                .pattern("G G")
                .define('B', Items.NETHERITE_BOOTS)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Dio's Diary
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.DIOS_DIARY.get())
                .pattern("PLP")
                .pattern("PNP")
                .pattern("PPP")
                .define('P', JItemRegistry.DIARY_PAGE.get())
                .define('L', Items.LEATHER)
                .define('N', Items.NETHER_STAR)
                .unlockedBy("has_nether_star", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHER_STAR))
                .unlockedBy("has_diary_page", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIARY_PAGE.get()))
                .save(exporter);
        // Diary Page
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, JItemRegistry.DIARY_PAGE.get())
                .requires(Items.WAXED_OXIDIZED_CUT_COPPER_SLAB)
                .requires(Items.GOLD_INGOT)
                .requires(Items.NETHERITE_INGOT)
                .requires(Items.EXPERIENCE_BOTTLE)
                .requires(Items.PAPER)
                .requires(Items.NAUTILUS_SHELL)
                .requires(Items.ENDER_EYE)
                .requires(Items.INK_SAC)
                .requires(Items.EMERALD)
                .unlockedBy("has_netherite_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
                .save(exporter);
        // Jotaro's P4 cap
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_P4_CAP.get())
                .pattern("WYW")
                .pattern("GHG")
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .define('Y', Items.YELLOW_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Jotaro's P4 jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_P4_JACKET.get())
                .pattern("W W")
                .pattern("GCG")
                .pattern("GWG")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Jotaro's P4 pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_P4_PANTS.get())
                .pattern("BBB")
                .pattern("GLG")
                .pattern("W W")
                .define('B', Items.BLACK_DYE)
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Jotaro's P4 boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOTARO_P4_BOOTS.get())
                .pattern("W W")
                .pattern("PNP")
                .pattern("G G")
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .define('P', Items.PURPLE_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Kira's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KIRA_WIG.get())
                .pattern("WYW")
                .pattern("YHY")
                .define('H', Items.NETHERITE_HELMET)
                .define('W', Items.WHEAT)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Kira's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KIRA_JACKET.get())
                .pattern("BPB")
                .pattern("BCB")
                .pattern("BYB")
                .define('B', Items.BLUE_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('P', Items.PURPLE_DYE)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Kira's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KIRA_PANTS.get())
                .pattern("BPB")
                .pattern("BLB")
                .pattern("B B")
                .define('B', Items.BLUE_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Kira's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KIRA_BOOTS.get())
                .pattern(" Y ")
                .pattern("YNY")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Kosaku Kira's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KOSAKU_WIG.get())
                .pattern("WBW")
                .pattern("BHB")
                .define('B', Items.BLACK_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Kosaku Kira's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KOSAKU_JACKET.get())
                .pattern("WGW")
                .pattern("WCW")
                .pattern("WYW")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.GREEN_DYE)
                .define('W', Items.WHITE_DYE)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Kosaku Kira's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KOSAKU_PANTS.get())
                .pattern("WWW")
                .pattern("GLG")
                .pattern("W W")
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Kosaku Kira's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KOSAKU_BOOTS.get())
                .pattern("PNP")
                .pattern("U U")
                .define('N', Items.NETHERITE_BOOTS)
                .define('P', Items.PINK_DYE)
                .define('U', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Final Kira's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.FINAL_KIRA_WIG.get())
                .pattern("WBW")
                .pattern("XHX")
                .define('B', Items.BLACK_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .define('W', Items.WHEAT)
                .define('X', Items.WHITE_DYE)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Final Kira's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.FINAL_KIRA_JACKET.get())
                .pattern("WGW")
                .pattern("WCW")
                .pattern("WWW")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.LIME_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Final Kira's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.FINAL_KIRA_PANTS.get())
                .pattern("GWG")
                .pattern("WLW")
                .pattern("W W")
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Final Kira's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.FINAL_KIRA_BOOTS.get())
                .pattern("GNG")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .define('G', Items.GREEN_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Giorno's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GIORNO_WIG.get())
                .pattern("WWW")
                .pattern(" H ")
                .define('H', Items.NETHERITE_HELMET)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Giorno's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GIORNO_JACKET.get())
                .pattern("P P")
                .pattern("PCP")
                .pattern("PPP")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('P', Items.PINK_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Giorno's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GIORNO_PANTS.get())
                .pattern("PPP")
                .pattern("PLP")
                .pattern("P P")
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('P', Items.PINK_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Giorno's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GIORNO_BOOTS.get())
                .pattern("BNB")
                .pattern("L L")
                .define('B', Items.BLUE_DYE)
                .define('L', Items.LIGHT_BLUE_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Risotto's cap
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RISOTTO_CAP.get())
                .pattern("BBB")
                .pattern("BHB")
                .pattern("GGG")
                .define('B', Items.BLACK_DYE)
                .define('G', Items.GOLD_NUGGET)
                .define('H', Items.LEATHER_HELMET)
                .unlockedBy("has_leather_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER_HELMET))
                .save(exporter);
        // Risotto's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RISOTTO_JACKET.get())
                .pattern("BBB")
                .pattern("BCB")
                .pattern("XGX")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.GOLD_INGOT)
                .define('X', Items.BLACK_CARPET)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Risotto's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RISOTTO_PANTS.get())
                .pattern("WBW")
                .pattern("BLB")
                .pattern("W W")
                .define('B', Items.BLACK_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Risotto's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RISOTTO_BOOTS.get())
                .pattern("BNB")
                .pattern("L L")
                .define('B', Items.BLACK_DYE)
                .define('L', Items.LIGHT_GRAY_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Doppio's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DOPPIO_WIG.get())
                .pattern("WPW")
                .pattern("PHP")
                .pattern(" W ")
                .define('H', Items.NETHERITE_HELMET)
                .define('P', Items.PINK_DYE)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Doppio's shirt
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DOPPIO_SHIRT.get())
                .pattern("PPP")
                .pattern("PCP")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Diavolo's wig
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIAVOLO_WIG.get())
                .pattern("WPW")
                .pattern("PHP")
                .pattern(" B ")
                .define('B', Items.BLACK_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .define('P', Items.PINK_DYE)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Diavolo's shirt
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIAVOLO_SHIRT.get())
                .pattern("B B")
                .pattern(" C ")
                .pattern("B B")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Diavolo's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIAVOLO_PANTS.get())
                .pattern("GGG")
                .pattern("PLP")
                .pattern("P P")
                .define('G', Items.GREEN_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Diavolo's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIAVOLO_BOOTS.get())
                .pattern("BNB")
                .pattern("L L")
                .define('B', Items.BLUE_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .define('L', Items.LIGHT_BLUE_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Johnny's cap
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOHNNY_CAP.get())
                .pattern(" L ")
                .pattern("WGW")
                .pattern("WHW")
                .define('G', Items.GOLD_INGOT)
                .define('H', Items.NETHERITE_HELMET)
                .define('L', Items.LIGHT_BLUE_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Johnny's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOHNNY_JACKET.get())
                .pattern("WFW")
                .pattern("LCM")
                .pattern("WPW")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('F', Items.FEATHER)
                .define('L', Items.LIGHT_BLUE_DYE)
                .define('M', Items.MAGENTA_DYE)
                .define('P', Items.PURPLE_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Johnny's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOHNNY_PANTS.get())
                .pattern("PPP")
                .pattern("PLP")
                .pattern("P P")
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Johnny's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.JOHNNY_BOOTS.get())
                .pattern("LNL")
                .pattern("L L")
                .define('L', Items.LIGHT_GRAY_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Gyro's hat
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GYRO_HAT.get())
                .pattern("SPS")
                .pattern("YHY")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('H', Items.NETHERITE_HELMET)
                .define('P', Items.GLASS_PANE)
                .define('S', Items.STRING)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Gyro's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GYRO_SHIRT.get())
                .pattern("GGG")
                .pattern("PCP")
                .pattern("BPB")
                .define('B', Items.BLUE_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('G', Items.GREEN_CARPET)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Gyro's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GYRO_PANTS.get())
                .pattern("YIY")
                .pattern("YLY")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('I', Items.IRON_INGOT)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Gyro's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.GYRO_BOOTS.get())
                .pattern("I I")
                .pattern("BNB")
                .pattern("G G")
                .define('B', Items.BROWN_DYE)
                .define('G', Items.GREEN_DYE)
                .define('I', Items.IRON_NUGGET)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Diego's hat
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIEGO_HAT.get())
                .pattern("YYY")
                .pattern("CHC")
                .pattern("S S")
                .define('C', Items.CYAN_DYE)
                .define('H', Items.LEATHER_HELMET)
                .define('S', Items.STRING)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_leather_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER_HELMET))
                .save(exporter);
        // Diego's shirt
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIEGO_SHIRT.get())
                .pattern("CCC")
                .pattern("YXY")
                .pattern("CCC")
                .define('C', Items.CYAN_DYE)
                .define('X', Items.NETHERITE_CHESTPLATE)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Diego's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIEGO_PANTS.get())
                .pattern("BYB")
                .pattern("YLY")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Diego's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.DIEGO_BOOTS.get())
                .pattern("I I")
                .pattern("BNB")
                .pattern("B B")
                .define('B', Items.BROWN_DYE)
                .define('I', Items.IRON_NUGGET)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Ringo's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RINGO_OUTFIT.get())
                .pattern("XXX")
                .pattern("WLW")
                .pattern("W W")
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('X', Items.WHITE_CARPET)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Ringo's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.RINGO_BOOTS.get())
                .pattern("BNB")
                .pattern("W W")
                .define('B', Items.BROWN_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Valentine's hat
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.VALENTINE_WIG.get())
                .pattern(" H ")
                .pattern("W W")
                .pattern("W W")
                .define('H', Items.NETHERITE_HELMET)
                .define('W', Items.WHEAT)
                .unlockedBy("has_netherite_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET))
                .save(exporter);
        // Valentine's jacket
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.VALENTINE_JACKET.get())
                .pattern("PPP")
                .pattern("PCP")
                .pattern("WPW")
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('P', Items.PINK_DYE)
                .define('W', Items.WHITE_DYE)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Valentine's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.VALENTINE_PANTS.get())
                .pattern("PMP")
                .pattern("PLP")
                .pattern("P P")
                .define('L', Items.NETHERITE_LEGGINGS)
                .define('M', Items.MAGENTA_DYE)
                .define('P', Items.PINK_DYE)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Valentine's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.VALENTINE_BOOTS.get())
                .pattern("PNP")
                .pattern("P P")
                .define('N', Items.NETHERITE_BOOTS)
                .define('P', Items.PURPLE_DYE)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);
        // Pucci's hat
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.PUCCIS_HAT.get())
                .pattern("BNB")
                .pattern("LCL")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.LEATHER_HELMET)
                .define('L', Items.LEATHER)
                .define('N', Items.GOLD_NUGGET)
                .unlockedBy("has_leather_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER_HELMET))
                .save(exporter);
        // Pucci's robe
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.PUCCI_ROBE.get())
                .pattern("BYB")
                .pattern("BCB")
                .pattern("WYW")
                .define('B', Items.BLACK_DYE)
                .define('C', Items.NETHERITE_CHESTPLATE)
                .define('Y', Items.YELLOW_DYE)
                .define('W', Items.BLACK_CARPET)
                .unlockedBy("has_netherite_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_CHESTPLATE))
                .save(exporter);
        // Pucci's pants
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.PUCCI_PANTS.get())
                .pattern("BBB")
                .pattern("GLG")
                .pattern("B B")
                .define('B', Items.BLACK_DYE)
                .define('G', Items.GRAY_DYE)
                .define('L', Items.NETHERITE_LEGGINGS)
                .unlockedBy("has_netherite_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_LEGGINGS))
                .save(exporter);
        // Pucci's boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.PUCCI_BOOTS.get())
                .pattern("BNB")
                .pattern("L L")
                .define('B', Items.BLUE_DYE)
                .define('L', Items.GRAY_DYE)
                .define('N', Items.NETHERITE_BOOTS)
                .unlockedBy("has_netherite_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_BOOTS))
                .save(exporter);

        // hot sand
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, JBlockRegistry.HOT_SAND_BLOCK.get(), 8)
                .pattern("SSS")
                .pattern("SMS")
                .pattern("SSS")
                .define('M', Items.MAGMA_BLOCK)
                .define('S', Items.SAND)
                .unlockedBy("has_sand", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SAND))
                .unlockedBy("has_magma_block", InventoryChangeTrigger.TriggerInstance.hasItems(Items.MAGMA_BLOCK))
                .save(exporter);
        // Anubis sheathed
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.ANUBIS_SHEATHED.get())
                .pattern("LSI")
                .pattern("SDS")
                .pattern("GSL")
                .define('D', Items.DIAMOND)
                .define('G', Items.GOLD_BLOCK)
                .define('L', Items.LEATHER)
                .define('I', Items.IRON_BLOCK)
                .define('S', JBlockRegistry.SOUL_BLOCK.get())
                .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .unlockedBy("has_soul_block", InventoryChangeTrigger.TriggerInstance.hasItems(JBlockRegistry.SOUL_BLOCK.get()))
                .save(exporter);
        // boxing gloves
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.BOXING_GLOVES.get())
                .pattern("LLR")
                .pattern("SLL")
                .pattern(" SL")
                .define('L', Items.LEATHER)
                .define('R', Items.RED_DYE)
                .define('S', Items.STRING)
                .unlockedBy("has_leather", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LEATHER))
                .save(exporter);
        // coin to nuggets
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.GOLD_NUGGET, 2)
                .requires(JItemRegistry.KQ_COIN.get())
                .unlockedBy("has_coin", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KQ_COIN.get()))
                .save(exporter);
        // nuggets to coin
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, JItemRegistry.KQ_COIN.get())
                .requires(Items.GOLD_NUGGET)
                .requires(Items.GOLD_NUGGET)
                .unlockedBy("has_nugget", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_NUGGET))
                .save(exporter);
        // green baby
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.GREEN_BABY.get())
                .pattern("GBG")
                .pattern("SMS")
                .pattern("GBG")
                .define('B', Items.BONE_BLOCK)
                .define('G', Items.GREEN_DYE)
                .define('M', Items.TOTEM_OF_UNDYING)
                .define('S', JBlockRegistry.SOUL_BLOCK.get())
                .unlockedBy("has_totem", InventoryChangeTrigger.TriggerInstance.hasItems(Items.TOTEM_OF_UNDYING))
                .save(exporter);
        // knife
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.KNIFE.get())
                .pattern("  N")
                .pattern(" I ")
                .pattern("S  ")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .define('S', Items.STICK)
                .unlockedBy("has_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(exporter);
        // knife bundle
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, JItemRegistry.KNIFEBUNDLE.get())
                .requires(JItemRegistry.KNIFE.get(), 9)
                .unlockedBy("has_knife", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KNIFE.get()))
                .save(exporter);
        // road roller
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, JItemRegistry.ROAD_ROLLER.get())
                .pattern("BYY")
                .pattern("III")
                .pattern("GFG")
                .define('B', Items.IRON_BARS)
                .define('I', Items.IRON_BLOCK)
                .define('Y', Items.YELLOW_DYE)
                .define('F', Items.BLAST_FURNACE)
                .define('G', Items.GRINDSTONE)
                .unlockedBy("has_iron_block", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_BLOCK))
                .save(exporter);
        // road roller
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.STEEL_BALL.get())
                .pattern(" I ")
                .pattern("ILI")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('L', Items.LIME_DYE)
                .unlockedBy("has_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(exporter);
        // training dummy
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.TRAINING_DUMMY.get())
                .pattern("CWC")
                .pattern("CWC")
                .pattern("CWC")
                .define('W', ItemTags.WOOL)
                .define('C', ItemTags.WOOL_CARPETS)
                .unlockedBy("has_wool", InventoryChangeTrigger.TriggerInstance.hasItems(ofTag(ItemTags.WOOL)))
                .unlockedBy("has_wool_carpet", InventoryChangeTrigger.TriggerInstance.hasItems(ofTag(ItemTags.WOOL_CARPETS)))
                .save(exporter);
    }

    public static ItemPredicate ofTag(TagKey<Item> tag) {
        return new ItemPredicate(tag,
                null,
                MinMaxBounds.Ints.ANY,
                MinMaxBounds.Ints.ANY,
                EnchantmentPredicate.NONE,
                EnchantmentPredicate.NONE,
                null,
                NbtPredicate.ANY
        );
    }
}
