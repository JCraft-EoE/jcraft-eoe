package net.arna.jcraft.datagen.providers.data;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JBlockRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.common.item.CosplayItem;
import net.arna.jcraft.datagen.builder.CrazyDiamondRecipeBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.function.Consumer;

public class JRecipeProvider extends FabricRecipeProvider {

    public static final Map<ArmorMaterial, Item> HELMET_MAP = Map.ofEntries(
            Map.entry(ArmorMaterials.LEATHER, Items.LEATHER_HELMET),
            Map.entry(ArmorMaterials.GOLD, Items.GOLDEN_HELMET),
            Map.entry(ArmorMaterials.CHAIN, Items.CHAINMAIL_HELMET),
            Map.entry(ArmorMaterials.IRON, Items.IRON_HELMET),
            Map.entry(ArmorMaterials.DIAMOND, Items.DIAMOND_HELMET),
            Map.entry(ArmorMaterials.TURTLE, Items.TURTLE_HELMET),
            Map.entry(ArmorMaterials.NETHERITE, Items.NETHERITE_HELMET)
    );
    public static final Map<ArmorMaterial, Item> CHESTPLATE_MAP = Map.ofEntries(
            Map.entry(ArmorMaterials.LEATHER, Items.LEATHER_CHESTPLATE),
            Map.entry(ArmorMaterials.GOLD, Items.GOLDEN_CHESTPLATE),
            Map.entry(ArmorMaterials.CHAIN, Items.CHAINMAIL_CHESTPLATE),
            Map.entry(ArmorMaterials.IRON, Items.IRON_CHESTPLATE),
            Map.entry(ArmorMaterials.DIAMOND, Items.DIAMOND_CHESTPLATE),
            Map.entry(ArmorMaterials.NETHERITE, Items.NETHERITE_CHESTPLATE)
    );
    public static final Map<ArmorMaterial, Item> LEGGINGS_MAP = Map.ofEntries(
            Map.entry(ArmorMaterials.LEATHER, Items.LEATHER_LEGGINGS),
            Map.entry(ArmorMaterials.GOLD, Items.GOLDEN_LEGGINGS),
            Map.entry(ArmorMaterials.CHAIN, Items.CHAINMAIL_LEGGINGS),
            Map.entry(ArmorMaterials.IRON, Items.IRON_LEGGINGS),
            Map.entry(ArmorMaterials.DIAMOND, Items.DIAMOND_LEGGINGS),
            Map.entry(ArmorMaterials.NETHERITE, Items.NETHERITE_LEGGINGS)
    );
    public static final Map<ArmorMaterial, Item> BOOTS_MAP = Map.ofEntries(
            Map.entry(ArmorMaterials.LEATHER, Items.LEATHER_BOOTS),
            Map.entry(ArmorMaterials.GOLD, Items.GOLDEN_BOOTS),
            Map.entry(ArmorMaterials.CHAIN, Items.CHAINMAIL_BOOTS),
            Map.entry(ArmorMaterials.IRON, Items.IRON_BOOTS),
            Map.entry(ArmorMaterials.DIAMOND, Items.DIAMOND_BOOTS),
            Map.entry(ArmorMaterials.NETHERITE, Items.NETHERITE_BOOTS)
    );
    public static final Map<ArmorItem.Type, Map<ArmorMaterial, Item>> TYPE_MAP = Map.of(
        ArmorItem.Type.HELMET, HELMET_MAP,
        ArmorItem.Type.CHESTPLATE, CHESTPLATE_MAP,
        ArmorItem.Type.LEGGINGS, LEGGINGS_MAP,
        ArmorItem.Type.BOOTS, BOOTS_MAP
    );

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
        // blood bottle
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, JItemRegistry.BLOOD_BOTTLE.get())
                .pattern(" B ")
                .pattern(" G ")
                .pattern("GGG")
                .define('B', ItemTags.BUTTONS)
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLASS))
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
        // steel ball
//        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, JItemRegistry.STEEL_BALL.get())
//                .pattern(" I ")
//                .pattern("ILI")
//                .pattern(" I ")
//                .define('I', Items.IRON_INGOT)
//                .define('L', Items.LIME_DYE)
//                .unlockedBy("has_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
//                .save(exporter);
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
        buildCosplayRecipes(exporter);
        buildCdRecipes(exporter);
    }

    public record SymbolItem(char symbol, Item item) {
        // intentionally left empty
    }

    public static void buildCosplayRecipes(Consumer<FinishedRecipe> exporter) {
        generateCosplayRecipes(JItemRegistry.STRAIZO_PONCHO, exporter, new String[] {
                "BBB",
                "BXB",
                " B "
        },
                new SymbolItem('B', Items.BLACK_WOOL));
        generateCosplayRecipes(JItemRegistry.KARS_HEADWRAP, exporter, new String[] {
                " X ",
                "L L",
                " B "
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('L', Items.LEATHER));
        generateCosplayRecipes(JItemRegistry.RED_HAT, exporter, new String[] {
                " R ",
                "LXL"
        },
                new SymbolItem('L', Items.LEATHER),
                new SymbolItem('R', Items.RED_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_P1_WIG, exporter, new String[] {
                " W ",
                "WXW"
        },
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.DIO_P1_JACKET, exporter, new String[] {
                "RPR",
                "RXR",
                "WBW"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL),
                new SymbolItem('R', Items.RED_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_P1_PANTS, exporter, new String[] {
                "RWR",
                "RXR",
                "R R"
        },
                new SymbolItem('R', Items.RED_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_P1_BOOTS, exporter, new String[] {
                "PXP",
                "B B"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_CAP, exporter, new String[] {
                "BYB",
                "BXB"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_JACKET, exporter, new String[] {
                "B B",
                "BXB",
                "BBB"
        },
                new SymbolItem('B', Items.BLACK_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_PANTS, exporter, new String[] {
                "YYY",
                "BXB",
                "B B"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_BOOTS, exporter, new String[] {
                "BXB",
                "B B"
        },
                new SymbolItem('B', Items.BLACK_WOOL));
        generateCosplayRecipes(JItemRegistry.KAKYOIN_WIG, exporter, new String[] {
                "WRW",
                "RXR"
        },
                new SymbolItem('R', Items.RED_WOOL),
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.KAKYOIN_COAT, exporter, new String[] {
                "GYG",
                "GXG",
                "GGG"
        },
                new SymbolItem('G', Items.GREEN_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.KAKYOIN_PANTS, exporter, new String[] {
                "GGG",
                "GXG",
                "G G"
        },
                new SymbolItem('G', Items.GREEN_WOOL));
        generateCosplayRecipes(JItemRegistry.KAKYOIN_BOOTS, exporter, new String[] {
                "BXB",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL));
        generateCosplayRecipes(JItemRegistry.POLNAREFF_WIG, exporter, new String[] {
                "W",
                "W",
                "X"
        },
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.POLNAREFF_SHIRT, exporter, new String[] {
                "  B",
                "BXB",
                "BBB"
        },
                new SymbolItem('B', Items.BLACK_WOOL));
        generateCosplayRecipes(JItemRegistry.POLNAREFF_PANTS, exporter, new String[] {
                "WRW",
                "WXW",
                "W W"
        },
                new SymbolItem('R', Items.RED_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.POLNAREFF_BOOTS, exporter, new String[] {
                "B B",
                "BXB"
        },
                new SymbolItem('B', Items.BLACK_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_HEADBAND, exporter, new String[] {
                "GXG"
        },
                new SymbolItem('G', Items.GREEN_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_JACKET, exporter, new String[] {
                "Y Y",
                "YXY",
                "YBY"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_CAPE, exporter, new String[] {
                "RLR",
                "LXL",
                "LLL"
        },
                new SymbolItem('L', Items.RED_CARPET),
                new SymbolItem('R', Items.RED_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_PANTS, exporter, new String[] {
                "GGG",
                "YXY",
                "Y Y"
        },
                new SymbolItem('G', Items.GREEN_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.DIO_BOOTS, exporter, new String[] {
                "YXY",
                "Y Y"
        },
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.OH_DIO_WIG, exporter, new String[] {
                "OWO",
                "WXW"
        },
                new SymbolItem('O', Items.ORANGE_WOOL),
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.OH_DIO_JACKET, exporter, new String[] {
                "PPP",
                "BXB",
                "GBG"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('G', Items.GRAY_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.OH_DIO_PANTS, exporter, new String[] {
                "LLL",
                "BXB",
                "B B"
        },
                new SymbolItem('B', Items.LIGHT_BLUE_WOOL),
                new SymbolItem('L', Items.LIGHT_GRAY_WOOL));
        generateCosplayRecipes(JItemRegistry.OH_DIO_BOOTS, exporter, new String[] {
                "LXL",
                "G G"
        },
                new SymbolItem('G', Items.GREEN_WOOL),
                new SymbolItem('L', Items.LIME_WOOL));
        generateCosplayRecipes(JItemRegistry.HEAVEN_ATTAINED_WIG, exporter, new String[] {
                "WWW",
                "WXW",
                "W W"
        },
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.HEAVEN_ATTAINED_SHIRT, exporter, new String[] {
                "WWW",
                "GXG",
                "U U"
        },
                new SymbolItem('G', Items.GOLD_INGOT),
                new SymbolItem('U', Items.WHITE_CARPET),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.HEAVEN_ATTAINED_PANTS, exporter, new String[] {
                "WWW",
                "GXG",
                "W W"
        },
                new SymbolItem('G', Items.GOLD_INGOT),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.HEAVEN_ATTAINED_BOOTS, exporter, new String[] {
                "GXG",
                "G G"
        },
                new SymbolItem('G', Items.GOLD_INGOT));
        generateCosplayRecipes(JItemRegistry.JOTARO_P4_CAP, exporter, new String[] {
                "WYW",
                "GXG"
        },
                new SymbolItem('G', Items.LIGHT_GRAY_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P4_JACKET, exporter, new String[] {
                "W W",
                "GXG",
                "GWG"
        },
                new SymbolItem('G', Items.LIGHT_GRAY_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P4_PANTS, exporter, new String[] {
                "BBB",
                "GXG",
                "W W"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('G', Items.LIGHT_GRAY_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P4_BOOTS, exporter, new String[] {
                "W W",
                "PXP",
                "G G"
        },
                new SymbolItem('G', Items.LIGHT_GRAY_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P6_CAP, exporter, new String[] {
                "LGB",
                "BXB"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('G', Items.GOLD_NUGGET),
                new SymbolItem('L', Items.LIME_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P6_JACKET, exporter, new String[] {
                "B B",
                "LXL",
                "BWB"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('L', Items.LIME_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P6_PANTS, exporter, new String[] {
                "YBY",
                "BXB",
                "Y Y"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.JOTARO_P6_BOOTS, exporter, new String[] {
                "O O",
                "YXY",
                "Y Y"
        },
                new SymbolItem('O', Items.ORANGE_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.KIRA_WIG, exporter, new String[] {
                "WYW",
                "YXY"
        },
                new SymbolItem('W', Items.WHEAT),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.KIRA_JACKET, exporter, new String[] {
                "BPB",
                "BXB",
                "BYB"
        },
                new SymbolItem('B', Items.BLUE_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.KIRA_PANTS, exporter, new String[] {
                "BPB",
                "BXB",
                "B B"
        },
                new SymbolItem('B', Items.BLUE_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.KIRA_BOOTS, exporter, new String[] {
                " Y ",
                "YXY",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.KOSAKU_WIG, exporter, new String[] {
                "WBW",
                "BXB"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.KOSAKU_JACKET, exporter, new String[] {
                "WGW",
                "WXW",
                "WYW"
        },
                new SymbolItem('G', Items.GREEN_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.KOSAKU_PANTS, exporter, new String[] {
                "WWW",
                "GXG",
                "W W"
        },
                new SymbolItem('G', Items.LIGHT_GRAY_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.KOSAKU_BOOTS, exporter, new String[] {
                "PXP",
                "U U"
        },
                new SymbolItem('P', Items.PINK_WOOL),
                new SymbolItem('U', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.FINAL_KIRA_WIG, exporter, new String[] {
                "WBW",
                "YXY"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('W', Items.WHEAT),
                new SymbolItem('Y', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.FINAL_KIRA_JACKET, exporter, new String[] {
                "WGW",
                "WXW",
                "WWW"
        },
                new SymbolItem('G', Items.LIME_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.FINAL_KIRA_PANTS, exporter, new String[] {
                "GWG",
                "WXW",
                "W W"
        },
                new SymbolItem('G', Items.LIGHT_GRAY_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.FINAL_KIRA_BOOTS, exporter, new String[] {
                "GXG",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('G', Items.GREEN_WOOL));
        generateCosplayRecipes(JItemRegistry.GIORNO_WIG, exporter, new String[] {
                "WWW",
                " X "
        },
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.GIORNO_JACKET, exporter, new String[] {
                "P P",
                "PXP",
                "PPP"
        },
                new SymbolItem('P', Items.PINK_WOOL));
        generateCosplayRecipes(JItemRegistry.GIORNO_PANTS, exporter, new String[] {
                "PPP",
                "PXP",
                "P P"
        },
                new SymbolItem('P', Items.PINK_WOOL));
        generateCosplayRecipes(JItemRegistry.GIORNO_BOOTS, exporter, new String[] {
                "BXB",
                "L L"
        },
                new SymbolItem('B', Items.BLUE_WOOL),
                new SymbolItem('L', Items.LIGHT_BLUE_WOOL));
        generateCosplayRecipes(JItemRegistry.RISOTTO_CAP, exporter, new String[] {
                "BBB",
                "BXB",
                "GGG"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('G', Items.GOLD_NUGGET));
        generateCosplayRecipes(JItemRegistry.RISOTTO_JACKET, exporter, new String[] {
                "BBB",
                "BXB",
                "ZGZ"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('G', Items.GOLD_INGOT),
                new SymbolItem('Z', Items.BLACK_CARPET));
        generateCosplayRecipes(JItemRegistry.RISOTTO_PANTS, exporter, new String[] {
                "WBW",
                "BXB",
                "W W"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.RISOTTO_BOOTS, exporter, new String[] {
                "BXB",
                "L L"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('L', Items.LIGHT_GRAY_WOOL));
        generateCosplayRecipes(JItemRegistry.DOPPIO_WIG, exporter, new String[] {
                "WPW",
                "PXP",
                " W "
        },
                new SymbolItem('P', Items.PINK_WOOL),
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.DOPPIO_SHIRT, exporter, new String[] {
                "PPP",
                "PXP",
        },
                new SymbolItem('P', Items.PINK_WOOL));
        generateCosplayRecipes(JItemRegistry.DIAVOLO_WIG, exporter, new String[] {
                "WPW",
                "PXP",
                " B "
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('P', Items.PINK_WOOL),
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.DIAVOLO_SHIRT, exporter, new String[] {
                "B B",
                " X ",
                "B B"
        },
                new SymbolItem('B', Items.BLACK_WOOL));
        generateCosplayRecipes(JItemRegistry.DIAVOLO_PANTS, exporter, new String[] {
                "GGG",
                "PXP",
                "P P"
        },
                new SymbolItem('G', Items.GREEN_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.DIAVOLO_BOOTS, exporter, new String[] {
                "BXB",
                "L L"
        },
                new SymbolItem('B', Items.BLUE_WOOL),
                new SymbolItem('L', Items.LIGHT_BLUE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOHNNY_CAP, exporter, new String[] {
                " L ",
                "WGW",
                "WXW"
        },
                new SymbolItem('G', Items.GOLD_INGOT),
                new SymbolItem('L', Items.LIGHT_BLUE_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOHNNY_JACKET, exporter, new String[] {
                "WFW",
                "LXM",
                "WPW"
        },
                new SymbolItem('F', Items.FEATHER),
                new SymbolItem('L', Items.LIGHT_BLUE_WOOL),
                new SymbolItem('M', Items.MAGENTA_WOOL),
                new SymbolItem('P', Items.PURPLE_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOHNNY_PANTS, exporter, new String[] {
                "PPP",
                "PXP",
                "P P"
        },
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.JOHNNY_BOOTS, exporter, new String[] {
                "LXL",
                "L L"
        },
                new SymbolItem('L', Items.LIGHT_GRAY_WOOL));
        generateCosplayRecipes(JItemRegistry.GYRO_HAT, exporter, new String[] {
                "SPS",
                "YXY",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('P', Items.GLASS_PANE),
                new SymbolItem('S', Items.STRING),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.GYRO_SHIRT, exporter, new String[] {
                "GGG",
                "PXP",
                "BPB"
        },
                new SymbolItem('B', Items.BLUE_WOOL),
                new SymbolItem('G', Items.GREEN_CARPET),
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.GYRO_PANTS, exporter, new String[] {
                "YIY",
                "YXY",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('I', Items.IRON_INGOT),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.GYRO_BOOTS, exporter, new String[] {
                "I I",
                "BXB",
                "G G"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('G', Items.GREEN_WOOL),
                new SymbolItem('I', Items.IRON_NUGGET));
        generateCosplayRecipes(JItemRegistry.DIEGO_HAT, exporter, new String[] {
                "YYY",
                "CXC",
                "S S"
        },
                new SymbolItem('C', Items.CYAN_WOOL),
                new SymbolItem('S', Items.STRING),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.DIEGO_SHIRT, exporter, new String[] {
                "CCC",
                "YXY",
                "CCC"
        },
                new SymbolItem('C', Items.CYAN_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.DIEGO_PANTS, exporter, new String[] {
                "BYB",
                "YXY",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL));
        generateCosplayRecipes(JItemRegistry.DIEGO_BOOTS, exporter, new String[] {
                "I I",
                "BXB",
                "B B"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('I', Items.IRON_NUGGET));
        generateCosplayRecipes(JItemRegistry.RINGO_OUTFIT, exporter, new String[] {
                "ZZZ",
                "WXW",
                "W W"
        },
                new SymbolItem('W', Items.WHITE_WOOL),
                new SymbolItem('Z', Items.WHITE_CARPET));
        generateCosplayRecipes(JItemRegistry.RINGO_BOOTS, exporter, new String[] {
                "BXB",
                "W W"
        },
                new SymbolItem('B', Items.BROWN_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.COWBOY_HAT, exporter, new String[] {
                "LNL",
                "LXL"
        },
                new SymbolItem('L', Items.LEATHER),
                new SymbolItem('N', Items.FEATHER));
        generateCosplayRecipes(JItemRegistry.COWBOY_PONCHO, exporter, new String[] {
                "LXL",
                "NLN",
                " N "
        },
                new SymbolItem('L', Items.GREEN_CARPET),
                new SymbolItem('N', Items.WHITE_CARPET));
        generateCosplayRecipes(JItemRegistry.COWBOY_GUNBELT_SPURS, exporter, new String[] {
                "LXK",
                "L  ",
                "NLN"
        },
                new SymbolItem('L', Items.LEATHER),
                new SymbolItem('K', Items.GOLD_NUGGET),
                new SymbolItem('N', Items.IRON_NUGGET));
        generateCosplayRecipes(JItemRegistry.VALENTINE_WIG, exporter, new String[] {
                " X ",
                "W W",
                "W W"
        },
                new SymbolItem('W', Items.WHEAT));
        generateCosplayRecipes(JItemRegistry.VALENTINE_JACKET, exporter, new String[] {
                "PPP",
                "PXP",
                "WPW"
        },
                new SymbolItem('P', Items.PINK_WOOL),
                new SymbolItem('W', Items.WHITE_WOOL));
        generateCosplayRecipes(JItemRegistry.VALENTINE_PANTS, exporter, new String[] {
                "PMP",
                "PXP",
                "P P"
        },
                new SymbolItem('M', Items.MAGENTA_WOOL),
                new SymbolItem('P', Items.PINK_WOOL));
        generateCosplayRecipes(JItemRegistry.VALENTINE_BOOTS, exporter, new String[] {
                "PXP",
                "P P"
        },
                new SymbolItem('P', Items.PURPLE_WOOL));
        generateCosplayRecipes(JItemRegistry.PUCCIS_HAT, exporter, new String[] {
                "BNB",
                "LXL"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('L', Items.LEATHER),
                new SymbolItem('N', Items.GOLD_NUGGET));
        generateCosplayRecipes(JItemRegistry.PUCCI_ROBE, exporter, new String[] {
                "BYB",
                "BXB",
                "ZYZ"
        },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('Y', Items.YELLOW_WOOL),
                new SymbolItem('Z', Items.BLACK_CARPET));
        generateCosplayRecipes(JItemRegistry.PUCCI_PANTS, exporter, new String[] {
                        "BBB",
                        "GXG",
                        "B B"
                },
                new SymbolItem('B', Items.BLACK_WOOL),
                new SymbolItem('G', Items.GRAY_WOOL));
        generateCosplayRecipes(JItemRegistry.PUCCI_BOOTS, exporter, new String[] {
                        "BXB",
                        "G G"
                },
                new SymbolItem('B', Items.BLUE_WOOL),
                new SymbolItem('G', Items.GRAY_WOOL));
    }

    /**
     *
     * @param cosplay
     * @param exporter
     * @param pattern
     * @param ingredients The item meanings for symbols; do NOT include X, for it marks the (tiered) armor piece
     */
    public static void generateCosplayRecipes(CosplayItem<?> cosplay, Consumer<FinishedRecipe> exporter, String[] pattern, SymbolItem... ingredients) {
        for (Map.Entry<ArmorMaterial, Item> entry : TYPE_MAP.get(cosplay.getSlot()).entrySet()) {
            final RegistrySupplier<? extends ArmorItem> cosplayItem = cosplay.get(entry.getKey());
            if (cosplayItem == null) {
                continue;
            }
            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, cosplayItem.get());
            for (String line : pattern) {
                builder = builder.pattern(line);
            }
            for (SymbolItem si : ingredients) {
                builder = builder.define(si.symbol(), si.item());
            }
            builder.define('X', entry.getValue()) // the armor piece to change into cosplay
                    .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(entry.getValue()).getPath(),
                            InventoryChangeTrigger.TriggerInstance.hasItems(entry.getValue()))
                    .save(exporter);
        }
    }

    public static void buildCdRecipes(Consumer<FinishedRecipe> exporter) {
        new CrazyDiamondRecipeBuilder(Blocks.STONE)
                .requires(Blocks.COBBLESTONE)
                .save(exporter, JCraft.id("crazy_diamond/stone"));
        new CrazyDiamondRecipeBuilder(Blocks.DEEPSLATE)
                .requires(Blocks.COBBLED_DEEPSLATE)
                .save(exporter, JCraft.id("crazy_diamond/deepslate"));
        new CrazyDiamondRecipeBuilder(Blocks.OAK_LOG)
                .requires(Blocks.STRIPPED_OAK_LOG)
                .save(exporter, JCraft.id("crazy_diamond/oak_log"));
        new CrazyDiamondRecipeBuilder(Blocks.OAK_WOOD)
                .requires(Blocks.STRIPPED_OAK_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/oak_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.BIRCH_LOG)
                .requires(Blocks.STRIPPED_BIRCH_LOG)
                .save(exporter, JCraft.id("crazy_diamond/birch_log"));
        new CrazyDiamondRecipeBuilder(Blocks.BIRCH_WOOD)
                .requires(Blocks.STRIPPED_BIRCH_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/birch_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.ACACIA_LOG)
                .requires(Blocks.STRIPPED_ACACIA_LOG)
                .save(exporter, JCraft.id("crazy_diamond/acacia_log"));
        new CrazyDiamondRecipeBuilder(Blocks.ACACIA_WOOD)
                .requires(Blocks.STRIPPED_ACACIA_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/acacia_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.SPRUCE_LOG)
                .requires(Blocks.STRIPPED_SPRUCE_LOG)
                .save(exporter, JCraft.id("crazy_diamond/spruce_log"));
        new CrazyDiamondRecipeBuilder(Blocks.SPRUCE_WOOD)
                .requires(Blocks.STRIPPED_SPRUCE_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/spruce_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.DARK_OAK_LOG)
                .requires(Blocks.STRIPPED_DARK_OAK_LOG)
                .save(exporter, JCraft.id("crazy_diamond/dark_oak_log"));
        new CrazyDiamondRecipeBuilder(Blocks.DARK_OAK_WOOD)
                .requires(Blocks.STRIPPED_DARK_OAK_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/dark_oak_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.JUNGLE_LOG)
                .requires(Blocks.STRIPPED_JUNGLE_LOG)
                .save(exporter, JCraft.id("crazy_diamond/jungle_log"));
        new CrazyDiamondRecipeBuilder(Blocks.JUNGLE_WOOD)
                .requires(Blocks.STRIPPED_JUNGLE_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/jungle_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.MANGROVE_LOG)
                .requires(Blocks.STRIPPED_MANGROVE_LOG)
                .save(exporter, JCraft.id("crazy_diamond/mangrove_log"));
        new CrazyDiamondRecipeBuilder(Blocks.MANGROVE_WOOD)
                .requires(Blocks.STRIPPED_MANGROVE_WOOD)
                .save(exporter, JCraft.id("crazy_diamond/mangrove_wood"));
        new CrazyDiamondRecipeBuilder(Blocks.BAMBOO_BLOCK)
                .requires(Blocks.STRIPPED_BAMBOO_BLOCK)
                .save(exporter, JCraft.id("crazy_diamond/bamboo_block"));
        new CrazyDiamondRecipeBuilder(Blocks.CRIMSON_STEM)
                .requires(Blocks.STRIPPED_CRIMSON_STEM)
                .save(exporter, JCraft.id("crazy_diamond/crimson_stem"));
        new CrazyDiamondRecipeBuilder(Blocks.CRIMSON_HYPHAE)
                .requires(Blocks.STRIPPED_CRIMSON_HYPHAE)
                .save(exporter, JCraft.id("crazy_diamond/crimson_hyphae"));
        new CrazyDiamondRecipeBuilder(Blocks.WARPED_STEM)
                .requires(Blocks.STRIPPED_WARPED_STEM)
                .save(exporter, JCraft.id("crazy_diamond/warped_stem"));
        new CrazyDiamondRecipeBuilder(Blocks.WARPED_HYPHAE)
                .requires(Blocks.STRIPPED_WARPED_HYPHAE)
                .save(exporter, JCraft.id("crazy_diamond/warped_hyphae"));
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
