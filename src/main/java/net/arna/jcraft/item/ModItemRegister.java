package net.arna.jcraft.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.item.custom.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class ModItemRegister {
    private static Item registerItem(String name, Item item) { return Registry.register(Registry.ITEM, new Identifier(JCraft.MOD_ID, name), item); }

    public static final Item STANDARROW = registerItem("stand_arrow",
            new StandArrowItem(new FabricItemSettings().rarity(Rarity.RARE).fireproof()));

    public static final Item STANDDISC = registerItem("stand_disc",
            new StandDiscItem(new FabricItemSettings().rarity(Rarity.RARE).fireproof().maxCount(1)));

    public static final Item FVREVOLVER = registerItem("fv_revolver",
            new FVRevolverItem(new FabricItemSettings().rarity(Rarity.UNCOMMON).maxDamage(1200)));

    public static final Item KQCOIN = registerItem("kq_coin",
            new KQCoinItem(new FabricItemSettings()));

    public static final Item GREENBABY = registerItem("green_baby",
            new GreenBabyItem(new FabricItemSettings().rarity(Rarity.RARE)));

    public static final Item DIOSDIARY = registerItem("dios_diary",
            new DIOsDiaryItem(new FabricItemSettings().rarity(Rarity.EPIC).fireproof()));

    public static final Item SINNERSSOUL = registerItem("sinners_soul",
            new SinnersSoulItem(new FabricItemSettings()));

    public static final Item KNIFE = registerItem("knife",
            new KnifeItem(new FabricItemSettings()));

    public static final Item KNIFEBUNDLE = registerItem("knife_bundle",
            new KnifeBundleItem(new FabricItemSettings().maxCount(1)));

    public static final Item ANUBIS = registerItem("anubis",
            new AnubisItem(new FabricItemSettings().rarity(Rarity.RARE).maxCount(1)));

    public static final Item ANUBISSHEATHED = registerItem("anubis_sheathed",
            new SheathedAnubisItem(new FabricItemSettings().rarity(Rarity.RARE).maxCount(1)));

    public static final Item KNUCKLEDUSTER = registerItem("knuckleduster",
            new KnuckledusterItem(new FabricItemSettings()));

    public static final Item BOXINGGLOVES = registerItem("boxing_gloves",
            new BoxingGlovesItem(new FabricItemSettings().maxCount(1)));

    public static final Item REQUIEMRUBY = registerItem("requiem_ruby",
            new Item(new FabricItemSettings().rarity(Rarity.EPIC).fireproof()));
    public static final Item REQUIEMARROW = registerItem("requiem_arrow",
            new RequiemArrowItem(new FabricItemSettings().rarity(Rarity.EPIC).fireproof()));

    public static final Item LIVINGARROW = registerItem("living_arrow",
            new LivingArrowItem(new FabricItemSettings().rarity(Rarity.RARE).fireproof()));

    public static final Item DIOHEADBAND = registerItem("dio_headband", new DIOArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.HEAD, new FabricItemSettings()));
    public static final Item DIOJACKET = registerItem("dio_jacket", new DIOArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.CHEST, new FabricItemSettings()));
    public static final Item DIOPANTS = registerItem("dio_pants", new DIOArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.LEGS, new FabricItemSettings()));
    public static final Item DIOBOOTS = registerItem("dio_boots", new DIOArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.FEET, new FabricItemSettings()));

    public static final Item JOTAROCAP = registerItem("jotaro_cap", new JotaroArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.HEAD, new FabricItemSettings()));
    public static final Item JOTAROJACKET = registerItem("jotaro_jacket", new JotaroArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.CHEST, new FabricItemSettings()));
    public static final Item JOTAROPANTS = registerItem("jotaro_pants", new JotaroArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.LEGS, new FabricItemSettings()));
    public static final Item JOTAROBOOTS = registerItem("jotaro_boots", new JotaroArmorItem(
            ArmorMaterials.NETHERITE, EquipmentSlot.FEET, new FabricItemSettings()));

    public static void RegisterModItems() {
        System.out.println("Registering Mod Items for " + JCraft.MOD_ID);
    }
}
