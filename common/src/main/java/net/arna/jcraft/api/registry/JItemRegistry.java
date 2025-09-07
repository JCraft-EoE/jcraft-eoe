package net.arna.jcraft.api.registry;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.vehicle.RoadRollerEntity;
import net.arna.jcraft.common.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface JItemRegistry {

    DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, Registries.ITEM);
    Map<RegistrySupplier<? extends Item>, ResourceLocation> ITEMS = new LinkedHashMap<>();

    RegistrySupplier<Item> DEBUG_WAND = Platform.isDevelopmentEnvironment() ? register("debug_wand", () -> new DebugWand(settings())) : null;

    RegistrySupplier<Item> STAND_ARROW = register("stand_arrow", () -> new StandArrowItem(settings().rarity(Rarity.RARE).fireResistant()));

    RegistrySupplier<Item> DISC = register("disc", () -> new Item(settings()));
    RegistrySupplier<Item> STAND_DISC = register("stand_disc", () -> new StandDiscItem(settings().fireResistant().stacksTo(1)));
    RegistrySupplier<Item> SPEC_DISC = register("spec_disc", () -> new SpecDiscItem(settings().fireResistant().stacksTo(1)));

    RegistrySupplier<Item> FV_REVOLVER = register("fv_revolver", () -> new FVRevolverItem(settings().rarity(Rarity.UNCOMMON).durability(1200)));
    RegistrySupplier<Item> PEACEMAKER = register("peacemaker", () -> new Peacemaker(settings().rarity(Rarity.RARE).stacksTo(1)));

    RegistrySupplier<Item> BULLET = register("bullet", () -> new BulletItem(settings()));

    RegistrySupplier<Item> KQ_COIN = register("kq_coin", () -> new KQCoinItem(settings()));

    RegistrySupplier<GreenBabyItem> GREEN_BABY = register("green_baby", () -> new GreenBabyItem(settings().rarity(Rarity.RARE)));

    RegistrySupplier<DiaryPageItem> DIARY_PAGE = register("diary_page", () -> new DiaryPageItem(settings().rarity(Rarity.UNCOMMON)));

    RegistrySupplier<DIOsDiaryItem> DIOS_DIARY = register("dios_diary", () -> new DIOsDiaryItem(settings().rarity(Rarity.EPIC).fireResistant()));

    RegistrySupplier<TrainingDummyItem> TRAINING_DUMMY = register("training_dummy", () -> new TrainingDummyItem(settings().stacksTo(1)));

    RegistrySupplier<Item> SINNERS_SOUL = register("sinners_soul", () -> new SinnersSoulItem(settings()));

    RegistrySupplier<Item> KNIFE = register("knife", () -> new KnifeItem(settings()));

    RegistrySupplier<Item> SCALPEL = register("scalpel", () -> new ScalpelItem(settings()));

    RegistrySupplier<Item> KNIFEBUNDLE = register("knife_bundle", () -> new KnifeBundleItem(settings().stacksTo(1)));

    RegistrySupplier<Item> ANUBIS = register("anubis", () -> new AnubisItem(settings().rarity(Rarity.RARE).stacksTo(1)));

    // Spec Obtainment Items
    RegistrySupplier<Item> ANUBIS_SHEATHED = register("anubis_sheathed", () -> new SheathedAnubisItem(settings()
            .rarity(Rarity.RARE).stacksTo(1), JSpecTypeRegistry.ANUBIS));

    RegistrySupplier<Item> BOXING_GLOVES = register("boxing_gloves", () -> new BoxingGlovesItem(settings()
            .stacksTo(1), JSpecTypeRegistry.BRAWLER));

    // Vehicles
    RegistrySupplier<Item> ROAD_ROLLER = register("road_roller", () -> new VehicleItem<>(
            settings().rarity(Rarity.RARE).stacksTo(1),
            (level, owner) -> new RoadRollerEntity(level)
    ));

    RegistrySupplier<Item> REQUIEM_RUBY = register("requiem_ruby", () -> new Item(settings().rarity(Rarity.EPIC).fireResistant()));

    RegistrySupplier<RequiemArrowItem> REQUIEM_ARROW = register("requiem_arrow", () -> new RequiemArrowItem(settings().rarity(Rarity.EPIC).fireResistant()));

    RegistrySupplier<LivingArrowItem> LIVING_ARROW = register("living_arrow", () -> new LivingArrowItem(settings().rarity(Rarity.RARE).fireResistant()));

    RegistrySupplier<Item> DIO_P1_WIG = register("dio_p1_wig", () -> new DioP1ClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> DIO_P1_JACKET = register("dio_p1_jacket", () -> new DioP1ClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> DIO_P1_PANTS = register("dio_p1_pants", () -> new DioP1ClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> DIO_P1_BOOTS = register("dio_p1_boots", () -> new DioP1ClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> STRAIZO_PONCHO = register("straizo_poncho", () -> new StraizoPonchoItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));

    RegistrySupplier<Item> DIO_HEADBAND = register("dio_headband", () -> new DIOJacketItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> DIO_JACKET = register("dio_jacket", () -> new DIOJacketItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> DIO_PANTS = register("dio_pants", () -> new DIOtardItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> DIO_BOOTS = register("dio_boots", () -> new DIOtardItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));
    RegistrySupplier<Item> DIO_CAPE = register("dio_cape", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));

    RegistrySupplier<Item> HEAVEN_ATTAINED_WIG = register("heaven_attained_wig", () -> new HeavenAttainedItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> HEAVEN_ATTAINED_SHIRT = register("heaven_attained_shirt", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> HEAVEN_ATTAINED_PANTS = register("heaven_attained_pants", () -> new HeavenAttainedItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> HEAVEN_ATTAINED_BOOTS = register("heaven_attained_boots", () -> new HeavenAttainedItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> KARS_HEADWRAP = register("kars_headwrap", () -> new HatItem(ArmorMaterials.IRON, settings()));
    RegistrySupplier<Item> RED_HAT = register("red_hat", () -> new HatItem(ArmorMaterials.IRON, settings()));
    RegistrySupplier<Item> PUCCIS_HAT = register("puccis_hat", () -> new HatItem(ArmorMaterials.IRON, settings()));

    RegistrySupplier<Item> STONE_MASK = register("stone_mask", () -> new StoneMaskItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, settings()));
    RegistrySupplier<Item> SHIV = register("shiv", () -> new ShivItem(Tiers.IRON, settings()));

    RegistrySupplier<Item> JOTARO_CAP = register("jotaro_cap", () -> new JotaroClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> JOTARO_JACKET = register("jotaro_jacket", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> JOTARO_PANTS = register("jotaro_pants", () -> new JotaroClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> JOTARO_BOOTS = register("jotaro_boots", () -> new JotaroClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> JOTARO_P4_CAP = register("jotaro_p4_cap", () -> new JotaroClothesP4Item(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> JOTARO_P4_JACKET = register("jotaro_p4_jacket", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> JOTARO_P4_PANTS = register("jotaro_p4_pants", () -> new JotaroClothesP4Item(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> JOTARO_P4_BOOTS = register("jotaro_p4_boots", () -> new JotaroClothesP4Item(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> KAKYOIN_WIG = register("kakyoin_wig", () -> new KakyoinClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> KAKYOIN_COAT = register("kakyoin_coat", () -> new KakyoinCoatItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> KAKYOIN_PANTS = register("kakyoin_pants", () -> new KakyoinClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> KAKYOIN_BOOTS = register("kakyoin_boots", () -> new KakyoinClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> KIRA_WIG = register("kira_wig", () -> new KiraOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> KIRA_JACKET = register("kira_jacket", () -> new KiraJacketItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> KIRA_PANTS = register("kira_pants", () -> new KiraOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> KIRA_BOOTS = register("kira_boots", () -> new KiraOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));
    RegistrySupplier<Item> KOSAKU_WIG = register("kosaku_wig", () -> new KosakuOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> KOSAKU_JACKET = register("kosaku_jacket", () -> new KosakuJacketItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> KOSAKU_PANTS = register("kosaku_pants", () -> new KosakuOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> KOSAKU_BOOTS = register("kosaku_boots", () -> new KosakuOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));
    RegistrySupplier<Item> FINAL_KIRA_WIG = register("final_kira_wig", () -> new FinalKiraOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> FINAL_KIRA_JACKET = register("final_kira_jacket", () -> new FinalKiraJacketItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> FINAL_KIRA_PANTS = register("final_kira_pants", () -> new FinalKiraOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> FINAL_KIRA_BOOTS = register("final_kira_boots", () -> new FinalKiraOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> GIORNO_WIG = register("giorno_wig", () -> new GiornoClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> GIORNO_JACKET = register("giorno_jacket", () -> new GiornoJacketItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> GIORNO_PANTS = register("giorno_pants", () -> new GiornoClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> GIORNO_BOOTS = register("giorno_boots", () -> new GiornoClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> RISOTTO_CAP = register("risotto_cap", () -> new RisottoCapItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, settings()));
    RegistrySupplier<Item> RISOTTO_JACKET = register("risotto_jacket", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> RISOTTO_PANTS = register("risotto_pants", () -> new RisottoBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> RISOTTO_BOOTS = register("risotto_boots", () -> new RisottoBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> DOPPIO_WIG = register("doppio_wig", () -> new DoppioClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> DOPPIO_SHIRT = register("doppio_shirt", () -> new DoppioClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));

    RegistrySupplier<Item> DIAVOLO_WIG = register("diavolo_wig", () -> new DiavoloClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> DIAVOLO_SHIRT = register("diavolo_shirt", () -> new DiavoloShirtItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> DIAVOLO_PANTS = register("diavolo_pants", () -> new DiavoloClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> DIAVOLO_BOOTS = register("diavolo_boots", () -> new DiavoloClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> PUCCI_ROBE = register("pucci_robe", () -> new PucciRobeItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> PUCCI_PANTS = register("pucci_pants", () -> new PucciBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> PUCCI_BOOTS = register("pucci_boots", () -> new PucciBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> JOHNNY_CAP = register("johnny_cap", () -> new JohnnyClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> JOHNNY_JACKET = register("johnny_jacket", () -> new JohnnyClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> JOHNNY_PANTS = register("johnny_pants", () -> new JohnnyClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> JOHNNY_BOOTS = register("johnny_boots", () -> new JohnnyClothesItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> GYRO_HAT = register("gyro_hat", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> GYRO_SHIRT = register("gyro_shirt", () -> new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> GYRO_PANTS = register("gyro_pants", () -> new GyroBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> GYRO_BOOTS = register("gyro_boots", () -> new GyroBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> DIEGO_HAT = register("diego_hat", () -> new DiegoOutfitItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, settings()));
    RegistrySupplier<Item> DIEGO_SHIRT = register("diego_shirt", () -> new DiegoOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> DIEGO_PANTS = register("diego_pants", () -> new DiegoOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> DIEGO_BOOTS = register("diego_boots", () -> new DiegoOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> RINGO_OUTFIT = register("ringo_outfit", () -> new RingoOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> RINGO_BOOTS = register("ringo_boots", () -> new RingoOutfitItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> VALENTINE_WIG = register("valentine_wig", () -> new ValentineTopItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings().fireResistant()));
    RegistrySupplier<Item> VALENTINE_JACKET = register("valentine_jacket", () -> new ValentineTopItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings().fireResistant()));
    RegistrySupplier<Item> VALENTINE_PANTS = register("valentine_pants", () -> new ValentineBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings().fireResistant()));
    RegistrySupplier<Item> VALENTINE_BOOTS = register("valentine_boots", () -> new ValentineBottomItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings().fireResistant()));

    RegistrySupplier<Item> CINDERELLA_MASK = register("cinderella_mask", CinderellaMaskItem::new);

    RegistrySupplier<Item> BLOOD_BOTTLE = register("blood_bottle", () -> new BloodBottleItem(settings().stacksTo(1)));

    RegistrySupplier<Item> STELLAR_IRON_INGOT = register("stellar_iron_ingot", () -> new Item(settings()));
    RegistrySupplier<Item> STAND_ARROWHEAD = register("stand_arrowhead", () -> new Item(settings()));
    RegistrySupplier<Item> PRISON_KEY = register("prison_key", () -> new Item(settings()));
    RegistrySupplier<Item> PLANKTON_VIAL = register("plankton_vial", () -> new Item(settings()));
    RegistrySupplier<Item> STEEL_BALL = register("steel_ball", () -> new Item(settings()));

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
    RegistrySupplier<Item> BRAWLER_SPAWN_EGG = register("brawler_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.BRAWLER_SPEC_USER, BASE_COLOR, PART_1_COLOR, settings()));
    RegistrySupplier<Item> VAMPIRE_SPAWN_EGG = register("vampire_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.VAMPIRE_SPEC_USER, PART_2_COLOR, PART_1_COLOR, settings()));
    RegistrySupplier<Item> ANUBIS_USER_SPAWN_EGG = register("anubis_user_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.ANUBIS_SPEC_USER, BASE_COLOR, PART_3_COLOR, settings()));
    RegistrySupplier<Item> PETSHOP_SPAWN_EGG = register("petshop_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.PETSHOP, BASE_COLOR, PART_3_COLOR, settings()));
    RegistrySupplier<Item> AYA_TSUJI_SPAWN_EGG = register("aya_tsuji_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.AYA_TSUJI, BASE_COLOR, PART_4_COLOR, settings()));
    RegistrySupplier<Item> DARBY_OLDER_SPAWN_EGG = register("darby_older_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.DARBY_OLDER, BASE_COLOR, PART_3_COLOR, settings()));
    RegistrySupplier<Item> DARBY_YOUNGER_SPAWN_EGG = register("darby_younger_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.DARBY_YOUNGER, BASE_COLOR, PART_3_COLOR, settings()));

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
