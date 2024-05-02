package net.arna.jcraft.datagen;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class JAdvancementProvider extends FabricAdvancementProvider {
    public JAdvancementProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        // obtain meteorite iron ore
        final Advancement obtainMeteoriteIronOre = Advancement.Builder.create()
                .display(JObjectRegistry.METEORITE_IRON_ORE_BLOCK,
                        Text.literal("On the Precipice of Greatness"),
                        Text.literal("Obtain Meteorite Iron Ore"),
                        JCraft.id("textures/block/foolish_sand_block.png"),
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .criterion("has_ore", InventoryChangedCriterion.Conditions.items(JObjectRegistry.METEORITE_IRON_ORE_BLOCK))
                .build(JCraft.id("obtain_meteorite_iron_ore"));
        consumer.accept(obtainMeteoriteIronOre);
        // obtain stand arrow
        final Advancement obtainStandArrow = Advancement.Builder.create()
                .display(JObjectRegistry.STANDARROW,
                        Text.literal("Stand Proud"),
                        Text.literal("Obtain a Stand Arrow"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .parent(obtainMeteoriteIronOre)
                .criterion("has_arrow", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STANDARROW))
                .build(JCraft.id("obtain_stand_arrow"));
        consumer.accept(obtainStandArrow);
        // obtain stand CD
        final Advancement obtainStandDisc = Advancement.Builder.create()
                .display(JObjectRegistry.STAND_DISC,
                        Text.literal("Spin Me Right Round"),
                        Text.literal("Obtain a Stand Disc"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .parent(obtainStandArrow)
                .criterion("has_disc", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STAND_DISC))
                .build(JCraft.id("obtain_stand_disc"));
        consumer.accept(obtainStandDisc);
        // obtain living arrow
        final Advancement obtainLivingArrow = Advancement.Builder.create()
                .display(JObjectRegistry.LIVINGARROW,
                        Text.literal("It's Alive!"),
                        Text.literal("Obtain a Living Arrow"),
                        null,
                        AdvancementFrame.GOAL,
                        true,
                        false,
                        false)
                .parent(obtainStandArrow)
                .criterion("has_arrow", InventoryChangedCriterion.Conditions.items(JObjectRegistry.LIVINGARROW))
                .build(JCraft.id("obtain_living_arrow"));
        consumer.accept(obtainLivingArrow);
        // obtain requiem arrow
        final Advancement obtainRequiemArrow = Advancement.Builder.create()
                .display(JObjectRegistry.REQUIEMARROW,
                        Text.literal("Requiem"),
                        Text.literal("Obtain a Requiem Arrow"),
                        null,
                        AdvancementFrame.CHALLENGE,
                        true,
                        false,
                        false)
                .parent(obtainStandArrow)
                .criterion("has_arrow", InventoryChangedCriterion.Conditions.items(JObjectRegistry.REQUIEMARROW))
                .build(JCraft.id("obtain_requiem_arrow"));
        consumer.accept(obtainRequiemArrow);
        // find stone mask
        final Advancement findStoneMask = Advancement.Builder.create()
                .display(JObjectRegistry.STONE_MASK,
                        Text.literal("This is gonna hurt…"),
                        Text.literal("Find a Stone Mask"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .parent(obtainMeteoriteIronOre)
                .criterion("has_mask", InventoryChangedCriterion.Conditions.items(JObjectRegistry.STONE_MASK))
                .build(JCraft.id("find_stone_mask"));
        consumer.accept(findStoneMask);
        // obtain coffin block
        final Advancement obtainCoffin = Advancement.Builder.create()
                .display(JObjectRegistry.COFFIN_BLOCK,
                        Text.literal("Sleepy Vampire"),
                        Text.literal("Obtain a Coffin"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .criterion("has_coffin", InventoryChangedCriterion.Conditions.items(JObjectRegistry.COFFIN_BLOCK))
                .build(JCraft.id("obtain_coffin"));
        consumer.accept(obtainCoffin);
        // obtain sun protections
        final Advancement obtainSunProtection = Advancement.Builder.create()
                .display(JObjectRegistry.KARSHEADWRAP,
                        Text.literal("Rise and Shine"),
                        Text.literal("Obtain all sun protection items"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .criterion("has_kars_headwrap", InventoryChangedCriterion.Conditions.items(JObjectRegistry.KARSHEADWRAP))
                .criterion("has_red_hat", InventoryChangedCriterion.Conditions.items(JObjectRegistry.RED_HAT))
                .build(JCraft.id("obtain_sun_protection"));
        consumer.accept(obtainSunProtection);
        // obtain blood bottle
        final Advancement obtainBloodBottle = Advancement.Builder.create()
                .display(JObjectRegistry.BLOOD_BOTTLE,
                        Text.literal("Not Kool-Aid"),
                        Text.literal("Obtain a blood bottle"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .criterion("has_bottle", InventoryChangedCriterion.Conditions.items(JObjectRegistry.BLOOD_BOTTLE))
                .build(JCraft.id("obtain_blood_bottle"));
        consumer.accept(obtainBloodBottle);
    }
}
