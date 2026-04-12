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

    RegistrySupplier<Item> SHIV = register("shiv", () -> new ShivItem(Tiers.IRON, settings()));

    RegistrySupplier<Item> STONE_MASK = register("stone_mask", () -> new StoneMaskItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, settings()));

    // the order of the cosplay here is the way it's ordered in the creative tab later

    CosplayItem<HatItem> RED_HAT = registerVampireHat("red_hat", (material, slot, settings) -> new HatItem(material, settings));

    CosplayItem<DioP1ClothesItem> DIO_P1_WIG = registerHelmet("dio_p1_wig", DioP1ClothesItem::new);
    CosplayItem<DioP1ClothesItem> DIO_P1_JACKET = registerChestplate("dio_p1_jacket", DioP1ClothesItem::new);
    CosplayItem<DioP1ClothesItem> DIO_P1_PANTS = registerLeggings("dio_p1_pants", DioP1ClothesItem::new);
    CosplayItem<DioP1ClothesItem> DIO_P1_BOOTS = registerBoots("dio_p1_boots", DioP1ClothesItem::new);

    CosplayItem<StraizoPonchoItem> STRAIZO_PONCHO = registerChestplate("straizo_poncho", StraizoPonchoItem::new);

    CosplayItem<HatItem> KARS_HEADWRAP = registerVampireHat("kars_headwrap", (material, slot, settings) -> new HatItem(material, settings));

    CosplayItem<DIOJacketItem> DIO_HEADBAND = registerHelmet("dio_headband", DIOJacketItem::new);
    CosplayItem<DIOJacketItem> DIO_JACKET = registerChestplate("dio_jacket", DIOJacketItem::new);
    CosplayItem<DIOtardItem> DIO_PANTS = registerLeggings("dio_pants", DIOtardItem::new);
    CosplayItem<DIOtardItem> DIO_BOOTS = registerBoots("dio_boots", DIOtardItem::new);
    CosplayItem<FlutteringArmorItem> DIO_CAPE = registerChestplate("dio_cape", FlutteringArmorItem::new);

    CosplayItem<ArmorItem> OH_DIO_WIG = registerHelmet("oh_dio_wig", ArmorItem::new);
    CosplayItem<ArmorItem> OH_DIO_JACKET = registerChestplate("oh_dio_jacket", ArmorItem::new);
    CosplayItem<ArmorItem> OH_DIO_PANTS = registerLeggings("oh_dio_pants", ArmorItem::new);
    CosplayItem<ArmorItem> OH_DIO_BOOTS = registerBoots("oh_dio_boots", ArmorItem::new);

    CosplayItem<HeavenAttainedItem> HEAVEN_ATTAINED_WIG = registerHelmet("heaven_attained_wig", HeavenAttainedItem::new);
    CosplayItem<FlutteringArmorItem> HEAVEN_ATTAINED_SHIRT = registerChestplate("heaven_attained_shirt", FlutteringArmorItem::new);
    CosplayItem<HeavenAttainedItem> HEAVEN_ATTAINED_PANTS = registerLeggings("heaven_attained_pants", HeavenAttainedItem::new);
    CosplayItem<HeavenAttainedItem> HEAVEN_ATTAINED_BOOTS = registerBoots("heaven_attained_boots", HeavenAttainedItem::new);

    CosplayItem<JotaroClothesItem> JOTARO_CAP = registerHelmet("jotaro_cap", JotaroClothesItem::new);
    CosplayItem<FlutteringArmorItem> JOTARO_JACKET = registerChestplate("jotaro_jacket", FlutteringArmorItem::new);
    CosplayItem<JotaroClothesItem> JOTARO_PANTS = registerLeggings("jotaro_pants", JotaroClothesItem::new);
    CosplayItem<JotaroClothesItem> JOTARO_BOOTS = registerBoots("jotaro_boots", JotaroClothesItem::new);

    CosplayItem<JotaroClothesP4Item> JOTARO_P4_CAP = registerHelmet("jotaro_p4_cap", JotaroClothesP4Item::new);
    CosplayItem<FlutteringArmorItem> JOTARO_P4_JACKET = registerChestplate("jotaro_p4_jacket", FlutteringArmorItem::new);
    CosplayItem<JotaroClothesP4Item> JOTARO_P4_PANTS = registerLeggings("jotaro_p4_pants", JotaroClothesP4Item::new);
    CosplayItem<JotaroClothesP4Item> JOTARO_P4_BOOTS = registerBoots("jotaro_p4_boots", JotaroClothesP4Item::new);

    CosplayItem<JotaroClothesP6Item> JOTARO_P6_CAP = registerHelmet("jotaro_p6_cap", JotaroClothesP6Item::new);
    CosplayItem<FlutteringArmorItem> JOTARO_P6_JACKET = registerChestplate("jotaro_p6_jacket", FlutteringArmorItem::new);
    CosplayItem<JotaroClothesP6Item> JOTARO_P6_PANTS = registerLeggings("jotaro_p6_pants", JotaroClothesP6Item::new);
    CosplayItem<JotaroClothesP6Item> JOTARO_P6_BOOTS = registerBoots("jotaro_p6_boots", JotaroClothesP6Item::new);

    CosplayItem<KakyoinClothesItem> KAKYOIN_WIG = registerHelmet("kakyoin_wig", KakyoinClothesItem::new);
    CosplayItem<KakyoinCoatItem> KAKYOIN_COAT = registerChestplate("kakyoin_coat", KakyoinCoatItem::new);
    CosplayItem<KakyoinClothesItem> KAKYOIN_PANTS = registerLeggings("kakyoin_pants", KakyoinClothesItem::new);
    CosplayItem<KakyoinClothesItem> KAKYOIN_BOOTS = registerBoots("kakyoin_boots", KakyoinClothesItem::new);

    CosplayItem<ArmorItem> POLNAREFF_WIG = registerHelmet("polnareff_wig", ArmorItem::new);
    CosplayItem<ArmorItem> POLNAREFF_SHIRT = registerChestplate("polnareff_shirt", ArmorItem::new);
    CosplayItem<ArmorItem> POLNAREFF_PANTS = registerLeggings("polnareff_pants", ArmorItem::new);
    CosplayItem<ArmorItem> POLNAREFF_BOOTS = registerBoots("polnareff_boots", ArmorItem::new);

    CosplayItem<KiraOutfitItem> KIRA_WIG = registerHelmet("kira_wig", KiraOutfitItem::new);
    CosplayItem<KiraJacketItem> KIRA_JACKET = registerChestplate("kira_jacket", KiraJacketItem::new);
    CosplayItem<KiraOutfitItem> KIRA_PANTS = registerLeggings("kira_pants", KiraOutfitItem::new);
    CosplayItem<KiraOutfitItem> KIRA_BOOTS = registerBoots("kira_boots", KiraOutfitItem::new);
    CosplayItem<KosakuOutfitItem> KOSAKU_WIG = registerHelmet("kosaku_wig", KosakuOutfitItem::new);
    CosplayItem<KosakuJacketItem> KOSAKU_JACKET = registerChestplate("kosaku_jacket", KosakuJacketItem::new);
    CosplayItem<KosakuOutfitItem> KOSAKU_PANTS = registerLeggings("kosaku_pants", KosakuOutfitItem::new);
    CosplayItem<KosakuOutfitItem> KOSAKU_BOOTS = registerBoots("kosaku_boots", KosakuOutfitItem::new);
    CosplayItem<FinalKiraOutfitItem> FINAL_KIRA_WIG = registerHelmet("final_kira_wig", FinalKiraOutfitItem::new);
    CosplayItem<FinalKiraJacketItem> FINAL_KIRA_JACKET = registerChestplate("final_kira_jacket", FinalKiraJacketItem::new);
    CosplayItem<FinalKiraOutfitItem> FINAL_KIRA_PANTS = registerLeggings("final_kira_pants", FinalKiraOutfitItem::new);
    CosplayItem<FinalKiraOutfitItem> FINAL_KIRA_BOOTS = registerBoots("final_kira_boots", FinalKiraOutfitItem::new);

    CosplayItem<GiornoClothesItem> GIORNO_WIG = registerHelmet("giorno_wig", GiornoClothesItem::new);
    CosplayItem<GiornoJacketItem> GIORNO_JACKET = registerChestplate("giorno_jacket", GiornoJacketItem::new);
    CosplayItem<GiornoClothesItem> GIORNO_PANTS = registerLeggings("giorno_pants", GiornoClothesItem::new);
    CosplayItem<GiornoClothesItem> GIORNO_BOOTS = registerBoots("giorno_boots", GiornoClothesItem::new);

    CosplayItem<RisottoCapItem> RISOTTO_CAP = registerVampireHat("risotto_cap", RisottoCapItem::new);
    CosplayItem<FlutteringArmorItem> RISOTTO_JACKET = registerChestplate("risotto_jacket", FlutteringArmorItem::new);
    CosplayItem<RisottoBottomItem> RISOTTO_PANTS = registerLeggings("risotto_pants", RisottoBottomItem::new);
    CosplayItem<RisottoBottomItem> RISOTTO_BOOTS = registerBoots("risotto_boots", RisottoBottomItem::new);

    CosplayItem<DoppioClothesItem> DOPPIO_WIG = registerHelmet("doppio_wig", DoppioClothesItem::new);
    CosplayItem<DoppioClothesItem> DOPPIO_SHIRT = registerChestplate("doppio_shirt", DoppioClothesItem::new);

    CosplayItem<DiavoloClothesItem> DIAVOLO_WIG = registerHelmet("diavolo_wig", DiavoloClothesItem::new);
    CosplayItem<DiavoloShirtItem> DIAVOLO_SHIRT = registerChestplate("diavolo_shirt", DiavoloShirtItem::new);
    CosplayItem<DiavoloClothesItem> DIAVOLO_PANTS = registerLeggings("diavolo_pants", DiavoloClothesItem::new);
    CosplayItem<DiavoloClothesItem> DIAVOLO_BOOTS = registerBoots("diavolo_boots", DiavoloClothesItem::new);

    CosplayItem<HatItem> PUCCIS_HAT = registerVampireHat("puccis_hat", (material, slot, settings) -> new HatItem(material, settings));
    CosplayItem<PucciRobeItem> PUCCI_ROBE = registerChestplate("pucci_robe", PucciRobeItem::new);
    CosplayItem<PucciBottomItem> PUCCI_PANTS = registerLeggings("pucci_pants", PucciBottomItem::new);
    CosplayItem<PucciBottomItem> PUCCI_BOOTS = registerBoots("pucci_boots", PucciBottomItem::new);

    CosplayItem<JohnnyClothesItem> JOHNNY_CAP = registerHelmet("johnny_cap", JohnnyClothesItem::new);
    CosplayItem<JohnnyClothesItem> JOHNNY_JACKET = registerChestplate("johnny_jacket", JohnnyClothesItem::new);
    CosplayItem<JohnnyClothesItem> JOHNNY_PANTS = registerLeggings("johnny_pants", JohnnyClothesItem::new);
    CosplayItem<JohnnyClothesItem> JOHNNY_BOOTS = registerBoots("johnny_boots", JohnnyClothesItem::new);

    CosplayItem<FlutteringArmorItem> GYRO_HAT = registerHelmet("gyro_hat", FlutteringArmorItem::new);
    CosplayItem<FlutteringArmorItem> GYRO_SHIRT = registerChestplate("gyro_shirt", FlutteringArmorItem::new);
    CosplayItem<GyroBottomItem> GYRO_PANTS = registerLeggings("gyro_pants", GyroBottomItem::new);
    CosplayItem<GyroBottomItem> GYRO_BOOTS = registerBoots("gyro_boots", GyroBottomItem::new);

    CosplayItem<DiegoOutfitItem> DIEGO_HAT = registerVampireHat("diego_hat", DiegoOutfitItem::new);
    CosplayItem<DiegoOutfitItem> DIEGO_SHIRT = registerChestplate("diego_shirt", DiegoOutfitItem::new);
    CosplayItem<DiegoOutfitItem> DIEGO_PANTS = registerLeggings("diego_pants", DiegoOutfitItem::new);
    CosplayItem<DiegoOutfitItem> DIEGO_BOOTS = registerBoots("diego_boots", DiegoOutfitItem::new);

    CosplayItem<ArmorItem> MOUNTAIN_TIM_HAT = registerVampireHat("mountain_tim_hat", ArmorItem::new);
    CosplayItem<ArmorItem> MOUNTAIN_TIM_SHIRT = registerChestplate("mountain_tim_shirt", ArmorItem::new);
    CosplayItem<FlutteringArmorItem> MOUNTAIN_TIM_COAT = registerChestplate("mountain_tim_coat", FlutteringArmorItem::new);
    CosplayItem<ArmorItem> MOUNTAIN_TIM_PANTS = registerLeggings("mountain_tim_pants", ArmorItem::new);
    CosplayItem<ArmorItem> MOUNTAIN_TIM_BOOTS = registerBoots("mountain_tim_boots", ArmorItem::new);

    CosplayItem<RingoOutfitItem> RINGO_OUTFIT = registerLeggings("ringo_outfit", RingoOutfitItem::new);
    CosplayItem<RingoOutfitItem> RINGO_BOOTS = registerBoots("ringo_boots", RingoOutfitItem::new);

    CosplayItem<ArmorItem> COWBOY_HAT = registerVampireHat("cowboy_hat", ArmorItem::new);
    CosplayItem<ArmorItem> COWBOY_PONCHO = registerChestplate("cowboy_poncho", ArmorItem::new);
    CosplayItem<ArmorItem> COWBOY_GUNBELT_SPURS = registerLeggings("cowboy_gunbelt_spurs", ArmorItem::new);

    CosplayItem<ValentineTopItem> VALENTINE_WIG = registerHelmet("valentine_wig", ValentineTopItem::new);
    CosplayItem<ValentineTopItem> VALENTINE_JACKET = registerChestplate("valentine_jacket", ValentineTopItem::new);
    CosplayItem<ValentineBottomItem> VALENTINE_PANTS = registerLeggings("valentine_pants", ValentineBottomItem::new);
    CosplayItem<ValentineBottomItem> VALENTINE_BOOTS = registerBoots("valentine_boots", ValentineBottomItem::new);

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
    RegistrySupplier<Item> HAMON_SPAWN_EGG = register("hamon_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.HAMON_SPEC_USER, BASE_COLOR, PART_1_COLOR, settings()));
    RegistrySupplier<Item> TONPETTY_SPAWN_EGG = register("tonpetty_spawn_egg",
            () -> new ArchitecturySpawnEggItem(JEntityTypeRegistry.TONPETTY, BASE_COLOR, PART_1_COLOR, settings()));
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

    RegistrySupplier<Item> AU_MOCK_ITEM = register("mock_item", AuMockItem::new);
    RegistrySupplier<Item> REWIND_MOCK_ITEM = register("rewind_mock_item", RewindMockItem::new);

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

    static <T extends ArmorItem> CosplayItem<T> registerVampireHat(String id, CosplayItem.CosplayItemConstructor<T> ctor) {
        return new CosplayItem<>(JCraft.MOD_ID, id, ArmorItem.Type.HELMET, true, ctor).register(JItemRegistry::register);
    }

    static <T extends ArmorItem> CosplayItem<T> registerHelmet(String id, CosplayItem.CosplayItemConstructor<T> ctor) {
        return new CosplayItem<>(JCraft.MOD_ID, id, ArmorItem.Type.HELMET, ctor).register(JItemRegistry::register);
    }

    static <T extends ArmorItem> CosplayItem<T> registerChestplate(String id, CosplayItem.CosplayItemConstructor<T> ctor) {
        return new CosplayItem<>(JCraft.MOD_ID, id, ArmorItem.Type.CHESTPLATE, ctor).register(JItemRegistry::register);
    }

    static <T extends ArmorItem> CosplayItem<T> registerLeggings(String id, CosplayItem.CosplayItemConstructor<T> ctor) {
        return new CosplayItem<>(JCraft.MOD_ID, id, ArmorItem.Type.LEGGINGS, ctor).register(JItemRegistry::register);
    }

    static <T extends ArmorItem> CosplayItem<T> registerBoots(String id, CosplayItem.CosplayItemConstructor<T> ctor) {
        return new CosplayItem<>(JCraft.MOD_ID, id, ArmorItem.Type.BOOTS, ctor).register(JItemRegistry::register);
    }

    static Item.Properties settings() {
        return new Item.Properties();
    }

    static void init() {}
}
