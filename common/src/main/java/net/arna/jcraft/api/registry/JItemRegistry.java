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

    RegistrySupplier<HatItem> RED_HAT = register("red_hat", () -> new HatItem(ArmorMaterials.IRON, settings()));

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

    static Item.Properties settings() {
        return new Item.Properties();
    }

    static void init() {}
}
