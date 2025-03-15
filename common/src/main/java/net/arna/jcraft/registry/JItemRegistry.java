package net.arna.jcraft.registry;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.item.*;
import net.arna.jcraft.common.spec.SpecType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface JItemRegistry {

    DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, Registries.ITEM);
    Map<RegistrySupplier<? extends Item>, ResourceLocation> ITEMS = new LinkedHashMap<>();

    RegistrySupplier<Item> DEBUG_WAND = register("debug_wand", () -> new DebugWand(settings()));

    RegistrySupplier<Item> STAND_ARROW = register("stand_arrow", () -> new StandArrowItem(settings().rarity(Rarity.RARE).fireResistant()));

    RegistrySupplier<Item> STAND_DISC = register("stand_disc", () -> new StandDiscItem(settings().rarity(Rarity.RARE).fireResistant().stacksTo(1)));

    RegistrySupplier<Item> BRAIN_DISC = register("brain_disc", () -> new BrainDiscItem(settings().rarity(Rarity.RARE).fireResistant().stacksTo(1)));

    RegistrySupplier<Item> FV_REVOLVER = register("fv_revolver", () -> new FVRevolverItem(settings().rarity(Rarity.UNCOMMON).durability(1200)));

    RegistrySupplier<Item> BULLET = register("bullet", () -> new BulletItem(settings()));

    RegistrySupplier<Item> KQ_COIN = register("kq_coin", () -> new KQCoinItem(settings()));

    RegistrySupplier<GreenBabyItem> GREEN_BABY = register("green_baby", () -> new GreenBabyItem(settings().rarity(Rarity.RARE)));

    RegistrySupplier<DiaryPageItem> DIARY_PAGE = register("diary_page", () -> new DiaryPageItem(settings().rarity(Rarity.UNCOMMON)));

    RegistrySupplier<DIOsDiaryItem> DIOS_DIARY = register("dios_diary", () -> new DIOsDiaryItem(settings().rarity(Rarity.EPIC).fireResistant()));


    RegistrySupplier<Item> SINNERS_SOUL = register("sinners_soul", () -> new SinnersSoulItem(settings()));

    RegistrySupplier<Item> KNIFE = register("knife", () -> new KnifeItem(settings()));

    RegistrySupplier<Item> SCALPEL = register("scalpel", () -> new ScalpelItem(settings()));

    RegistrySupplier<Item> KNIFEBUNDLE = register("knife_bundle", () -> new KnifeBundleItem(settings().stacksTo(1)));

    RegistrySupplier<Item> ANUBIS = register("anubis", () -> new AnubisItem(settings().rarity(Rarity.RARE).stacksTo(1)));

    // Spec Obtainment Items
    RegistrySupplier<Item> ANUBIS_SHEATHED = register("anubis_sheathed", () -> new SheathedAnubisItem(settings().rarity(Rarity.RARE).stacksTo(1), SpecType.ANUBIS));

    RegistrySupplier<Item> BOXING_GLOVES = register("boxing_gloves", () -> new BoxingGlovesItem(settings().stacksTo(1), SpecType.BRAWLER));


    RegistrySupplier<Item> REQUIEM_RUBY = register("requiem_ruby", () -> new Item(settings().rarity(Rarity.EPIC).fireResistant()));

    RegistrySupplier<RequiemArrowItem> REQUIEM_ARROW = register("requiem_arrow", () -> new RequiemArrowItem(settings().rarity(Rarity.EPIC).fireResistant()));

    RegistrySupplier<LivingArrowItem> LIVING_ARROW = register("living_arrow", () -> new LivingArrowItem(settings().rarity(Rarity.RARE).fireResistant()));

    RegistrySupplier<Item> DIO_HEADBAND = register("dio_headband", () -> new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings()));
    RegistrySupplier<Item> DIO_JACKET = register("dio_jacket", () -> new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings()));
    RegistrySupplier<Item> DIO_PANTS = register("dio_pants", () -> new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings()));
    RegistrySupplier<Item> DIO_BOOTS = register("dio_boots", () -> new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings()));

    RegistrySupplier<Item> DIO_CAPE = register("dio_cape", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings()));

    RegistrySupplier<Item> KARS_HEADWRAP = register("kars_headwrap", () -> new HatItem(ArmorMaterials.IRON, settings()));
    RegistrySupplier<Item> RED_HAT = register("red_hat", () -> new HatItem(ArmorMaterials.IRON, settings()));
    RegistrySupplier<Item> PUCCIS_HAT = register("puccis_hat", () -> new HatItem(ArmorMaterials.IRON, settings()));

    RegistrySupplier<Item> STONE_MASK = register("stone_mask", () -> new StoneMaskItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, settings()));
    RegistrySupplier<Item> SHIV = register("shiv", () -> new ShivItem(Tiers.IRON, settings()));

    RegistrySupplier<Item> JOTARO_CAP = register("jotaro_cap", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings()));
    RegistrySupplier<Item> JOTARO_JACKET = register("jotaro_jacket", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings()));
    RegistrySupplier<Item> JOTARO_PANTS = register("jotaro_pants", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings()));
    RegistrySupplier<Item> JOTARO_BOOTS = register("jotaro_boots", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings()));

    RegistrySupplier<Item> CINDERELLA_MASK = register("cinderella_mask", CinderellaMaskItem::new);

    RegistrySupplier<Item> BLOOD_BOTTLE = register("blood_bottle", () -> new BloodBottleItem(settings().stacksTo(1)));

    RegistrySupplier<Item> STELLAR_IRON_INGOT = register("stellar_iron_ingot", () -> new Item(settings()));
    RegistrySupplier<Item> STAND_ARROWHEAD = register("stand_arrowhead", () -> new Item(settings()));
    RegistrySupplier<Item> PRISON_KEY = register("prison_key", () -> new Item(settings()));
    RegistrySupplier<Item> PLANKTON_VIAL = register("plankton_vial", () -> new Item(settings()));

    int BASE_COLOR = 0xe8cc23;
    int PART_1_COLOR = 0xca7218;
    int PART_2_COLOR = 0x2e8f04;
    int PART_3_COLOR = 0x8710c4;
    int PART_4_COLOR = 0x3572db;
    int PART_5_COLOR = 0xe96de9;
    int PART_6_COLOR = 0x75cf1a;
    int PART_7_COLOR = 0x6de97b;
    int PART_8_COLOR = 0x4c9ad5;
    int PART_9_COLOR = 0xafd2d0;
    RegistrySupplier<Item> PETSHOP_SPAWN_EGG = register("petshop_spawn_egg", () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.PETSHOP, BASE_COLOR, PART_3_COLOR, settings()));
    RegistrySupplier<Item> AYA_TSUJI_SPAWN_EGG = register("aya_tsuji_spawn_egg", () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.AYA_TSUJI, BASE_COLOR, PART_4_COLOR, settings()));
    RegistrySupplier<Item> DARBY_OLDER_SPAWN_EGG = register("darby_older_spawn_egg", () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.DARBY_OLDER, BASE_COLOR, PART_3_COLOR, settings()));
    RegistrySupplier<Item> DARBY_YOUNGER_SPAWN_EGG = register("darby_younger_spawn_egg", () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.DARBY_YOUNGER, BASE_COLOR, PART_3_COLOR, settings()));

    RegistrySupplier<Item> MOCK_ITEM = register("mock_item", MockItem::new);

    //Block
    RegistrySupplier<Item> FOOLISH_SAND_BLOCK = register("foolish_sand_block",
            () -> new BlockItem(JBlockRegistry.FOOLISH_SAND_BLOCK.get(), settings()));
    RegistrySupplier<Item> SOUL_BLOCK = register("soul_block", () -> new BlockItem(JBlockRegistry.SOUL_BLOCK.get(), settings()));
    RegistrySupplier<Item> METEORITE_BLOCK = register("meteorite_block", () -> new BlockItem(JBlockRegistry.METEORITE_BLOCK.get(), settings()));
    RegistrySupplier<Item> POLISHED_METEORITE_BLOCK = register("polished_meteorite_block", () -> new BlockItem(JBlockRegistry.POLISHED_METEORITE_BLOCK.get(), settings()));
    RegistrySupplier<Item> METEORITE_IRON_ORE_BLOCK = register("meteorite_iron_ore_block",
            () -> new BlockItem(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get(), settings()));
    RegistrySupplier<Item> STELLAR_IRON_BLOCK = register("stellar_iron_block",
            () -> new BlockItem(JBlockRegistry.STELLAR_IRON_BLOCK.get(), settings()));
    RegistrySupplier<Item> HOT_SAND_BLOCK = register("hot_sand_block",
            () -> new BlockItem(JBlockRegistry.HOT_SAND_BLOCK.get(), settings()));
    RegistrySupplier<Item> CINDERELLA_GREEN_BLOCK = register("cinderella_green_block",
            () -> new BlockItem(JBlockRegistry.CINDERELLA_GREEN_BLOCK.get(), settings()));
    RegistrySupplier<Item> SOUL_WOOD_BLOCK = register("soul_wood_block",
            () -> new BlockItem(JBlockRegistry.SOUL_WOOD_BLOCK.get(), settings()));
    RegistrySupplier<Item> COFFIN_BLOCK = register("coffin",
            () -> new BlockItem(JBlockRegistry.COFFIN_BLOCK.get(), settings()));

    static <T extends Item> RegistrySupplier<T> register(String id, Supplier<? extends T> supplier) {
        RegistrySupplier<T> item = ITEM_REGISTRY.register(id, supplier);
        ITEMS.put(item, JCraft.id(id));
        return item;
    }

    static Item.Properties settings() {
        return new Item.Properties();
    }

    static void init() {}
}
