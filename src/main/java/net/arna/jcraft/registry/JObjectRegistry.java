package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.common.block.FoolishSandBlock;
import net.arna.jcraft.common.block.ShaderTestBlock;
import net.arna.jcraft.common.block.SoulBlock;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.common.item.*;
import net.arna.jcraft.common.spec.SpecType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;

public interface JObjectRegistry {
    Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    Map<Item, Identifier> ITEMS = new LinkedHashMap<>();

    Item DEBUG_WAND = FabricLoader.getInstance().isDevelopmentEnvironment() ? register("debug_wand", new DebugWand(settings())) : null;

    Item STANDARROW = register("stand_arrow", new StandArrowItem(settings().rarity(Rarity.RARE).fireproof()));

    Item STAND_DISC = register("stand_disc", new StandDiscItem(settings().rarity(Rarity.RARE).fireproof().maxCount(1)));

    Item FV_REVOLVER = register("fv_revolver", new FVRevolverItem(settings().rarity(Rarity.UNCOMMON).maxDamage(1200)));

    Item BULLET = register("bullet", new BulletItem(settings()));

    Item KQ_COIN = register("kq_coin", new KQCoinItem(settings()));

    Item GREENBABY = register("green_baby", new GreenBabyItem(settings().rarity(Rarity.RARE)));

    Item DIOSDIARY = register("dios_diary", new DIOsDiaryItem(settings().rarity(Rarity.EPIC).fireproof()));

    Item SINNERSSOUL = register("sinners_soul", new SinnersSoulItem(settings()));

    Item KNIFE = register("knife", new KnifeItem(settings()));

    Item KNIFEBUNDLE = register("knife_bundle", new KnifeBundleItem(settings().maxCount(1)));

    Item ANUBIS = register("anubis", new AnubisItem(settings().rarity(Rarity.RARE).maxCount(1)));

    // Spec Obtainment Items
    Item ANUBISSHEATHED = register("anubis_sheathed", new SheathedAnubisItem(settings().rarity(Rarity.RARE).maxCount(1), SpecType.ANUBIS));

    Item BOXINGGLOVES = register("boxing_gloves", new BoxingGlovesItem(settings().maxCount(1), SpecType.BRAWLER));


    Item REQUIEMRUBY = register("requiem_ruby", new Item(settings().rarity(Rarity.EPIC).fireproof()));

    Item REQUIEMARROW = register("requiem_arrow", new RequiemArrowItem(settings().rarity(Rarity.EPIC).fireproof()));

    Item LIVINGARROW = register("living_arrow", new LivingArrowItem(settings().rarity(Rarity.RARE).fireproof()));

    Item DIOHEADBAND = register("dio_headband", new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings()));
    Item DIOJACKET = register("dio_jacket", new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings()));
    Item DIOPANTS = register("dio_pants", new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings()));
    Item DIOBOOTS = register("dio_boots", new DIOArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings()));

    Item DIOCAPE = register("dio_cape", new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings()));

    Item KARSHEADWRAP = register("kars_headwrap", new SunProtectionItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, settings()));

    Item RED_HAT = register("red_hat", new SunProtectionItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, settings()));

    Item STONE_MASK = register("stone_mask", new StoneMaskItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, settings()));

    Item JOTAROCAP = register("jotaro_cap", new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, settings()));
    Item JOTAROJACKET = register("jotaro_jacket", new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, settings()));
    Item JOTAROPANTS = register("jotaro_pants", new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, settings()));
    Item JOTAROBOOTS = register("jotaro_boots", new FlutteringArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, settings()));

    Item CINDERELLA_MASK = register("cinderella_mask", new CinderellaMaskItem());

    Item BLOOD_BOTTLE = register("blood_bottle", new BloodBottleItem(settings().maxCount(1)));

    Item STELLAR_IRON_INGOT = register("stellar_iron_ingot", new Item(settings()));
    Item STAND_ARROWHEAD = register("stand_arrowhead", new Item(settings()));

    Item MOCK_ITEM = register("mock_item", new MockItem());

    //Block
    Block FOOLISH_SAND_BLOCK = register("foolish_sand_block", new FoolishSandBlock(FabricBlockSettings.create()
            .strength(0.5f)
            .sounds(BlockSoundGroup.SAND)
    ), settings(), true);
    Block SOUL_BLOCK = register("soul_block", new SoulBlock(FabricBlockSettings.create()
            .strength(4.0f)
            .sounds(BlockSoundGroup.SOUL_SOIL)
    ), settings(), true);
    Block METEORITE_BLOCK = register("meteorite_block", new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
            .requiresTool()
            .strength(6.0f, 1200f)
            .sounds(BlockSoundGroup.ANCIENT_DEBRIS)
    ), settings(), true);
    Block METEORITE_IRON_ORE_BLOCK = register("meteorite_iron_ore_block", new Block(FabricBlockSettings.copyOf(Blocks.IRON_ORE)
            .requiresTool()
            .strength(9.0f, 1200f)
            .sounds(BlockSoundGroup.ANCIENT_DEBRIS)
    ), settings(), true);
    Block COFFIN_BLOCK = register("coffin", new CoffinBlock(FabricBlockSettings.create().sounds(BlockSoundGroup.WOOD).nonOpaque()), settings(), true);
    Block SHADER_TEST_BLOCK = register("shader_test_block", new ShaderTestBlock(FabricBlockSettings.create()), settings(), true);

    static Item.Settings settings() {
        return new Item.Settings();
    }

    static <T extends Item> T register(String name, T item) {
        ITEMS.put(item, JCraft.id(name));
        return item;
    }

    static <T extends Block> T register(String name, T block, Item.Settings settings, boolean createItem) {
        BLOCKS.put(block, JCraft.id(name));
        if (createItem) {
            ITEMS.put(new BlockItem(block, settings), BLOCKS.get(block));
        }
        return block;
    }

    static void init() {
        BLOCKS.keySet().forEach(block -> Registry.register(Registries.BLOCK, BLOCKS.get(block), block));
        ITEMS.keySet().forEach(item -> Registry.register(Registries.ITEM, ITEMS.get(item), item));

        DispenserBlock.registerBehavior(KNIFE, new ProjectileDispenserBehavior() {
            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                KnifeProjectile knife = new KnifeProjectile(world);
                knife.setPosition(position.getX(), position.getY(), position.getZ());
                return knife;
            }
        });
    }
}
