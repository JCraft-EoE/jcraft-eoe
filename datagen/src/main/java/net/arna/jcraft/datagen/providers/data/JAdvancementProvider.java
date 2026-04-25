package net.arna.jcraft.datagen.providers.data;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.*;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.advancements.Hamon1Trigger;
import net.arna.jcraft.common.advancements.Hamon2Trigger;
import net.arna.jcraft.common.advancements.Hamon3Trigger;
import net.arna.jcraft.common.advancements.Hamon4Trigger;
import net.arna.jcraft.common.advancements.Hamon5Trigger;
import net.arna.jcraft.common.advancements.Hamon6Trigger;
import net.arna.jcraft.common.advancements.ObtainedSpecTrigger;
import net.arna.jcraft.common.advancements.ObtainedStandTrigger;
import net.arna.jcraft.common.item.SpecDiscItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class JAdvancementProvider extends FabricAdvancementProvider {
    public JAdvancementProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        // obtain meteorite iron ore
        final Advancement obtainMeteoriteIronOre = Advancement.Builder.advancement()
                .display(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get(),
                        Component.translatable("advancements.jcraft.obtain_meteorite_iron_ore.title"),
                        Component.translatable("advancements.jcraft.obtain_meteorite_iron_ore.description"),
                        JCraft.id("textures/block/foolish_sand_block.png"),
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .addCriterion("has_ore", InventoryChangeTrigger.TriggerInstance.hasItems(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get()))
                .build(JCraft.id("obtain_meteorite_iron_ore"));
        consumer.accept(obtainMeteoriteIronOre);
        // obtain stand
        final Advancement obtainStand = Advancement.Builder.advancement()
                .display(JItemRegistry.STAND_ARROW.get(),
                        Component.translatable("advancements.jcraft.obtain_stand.title"),
                        Component.translatable("advancements.jcraft.obtain_stand.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(obtainMeteoriteIronOre)
                .addCriterion("has_stand", ObtainedStandTrigger.TriggerInstance.obtainedStand())
                .rewards(AdvancementRewards.Builder.recipe(JCraft.id("disc")))
                .build(JCraft.id("obtain_stand"));
        consumer.accept(obtainStand);
        // obtain stand disc
        final Advancement obtainStandDisc = Advancement.Builder.advancement()
                .display(JItemRegistry.STAND_DISC.get(),
                        Component.translatable("advancements.jcraft.obtain_stand_disc.title"),
                        Component.translatable("advancements.jcraft.obtain_stand_disc.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(obtainStand)
                .addCriterion("has_disc", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STAND_DISC.get()))
                .build(JCraft.id("obtain_stand_disc"));
        consumer.accept(obtainStandDisc);
        // obtain "all" stands
        // stand data is not available during datagen so we have to list all stands needed for the achievement
        final var obtainableStands = List.of(
                JStandTypeRegistry.STAR_PLATINUM,
                JStandTypeRegistry.STAR_PLATINUM_THE_WORLD,
                JStandTypeRegistry.MAGICIANS_RED,
                JStandTypeRegistry.THE_WORLD,
                JStandTypeRegistry.KING_CRIMSON,
                JStandTypeRegistry.D4C,
                JStandTypeRegistry.CREAM,
                JStandTypeRegistry.KILLER_QUEEN,
                JStandTypeRegistry.WHITE_SNAKE,
                JStandTypeRegistry.SILVER_CHARIOT,
                JStandTypeRegistry.THE_FOOL,
                JStandTypeRegistry.GOLD_EXPERIENCE,
                JStandTypeRegistry.HIEROPHANT_GREEN,
                JStandTypeRegistry.THE_SUN,
                JStandTypeRegistry.PURPLE_HAZE,
                JStandTypeRegistry.C_MOON,
                JStandTypeRegistry.MADE_IN_HEAVEN,
                JStandTypeRegistry.THE_WORLD_OVER_HEAVEN,
                JStandTypeRegistry.KILLER_QUEEN_BITES_THE_DUST,
                JStandTypeRegistry.GOLD_EXPERIENCE_REQUIEM,
                JStandTypeRegistry.PURPLE_HAZE_DISTORTION,
                JStandTypeRegistry.HORUS,
                JStandTypeRegistry.SHADOW_THE_WORLD,
                JStandTypeRegistry.METALLICA,
                JStandTypeRegistry.THE_HAND,
                JStandTypeRegistry.MANDOM,
                JStandTypeRegistry.AEROSMITH
                //, JStandTypeRegistry.CRAZY_DIAMOND
        );
        final Advancement.Builder obtainAllStandsBuilder = Advancement.Builder.advancement()
                .display(JItemRegistry.STAND_DISC.get(),
                        Component.translatable("advancements.jcraft.obtain_all_stands.title"),
                        Component.translatable("advancements.jcraft.obtain_all_stands.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainStandDisc)
                .rewards(AdvancementRewards.Builder.experience(1395)); // that's from level 0 to 30
        for (final RegistrySupplier<StandType> type : obtainableStands) {
            obtainAllStandsBuilder.addCriterion("has_" + type.getId().getPath(), ObtainedStandTrigger.TriggerInstance.obtainedStand(type.get()));
        }
        consumer.accept(obtainAllStandsBuilder.build(JCraft.id("obtain_all_stands")));
        // obtain living arrow
        final Advancement obtainLivingArrow = Advancement.Builder.advancement()
                .display(JItemRegistry.LIVING_ARROW.get(),
                        Component.translatable("advancements.jcraft.obtain_living_arrow.title"),
                        Component.translatable("advancements.jcraft.obtain_living_arrow.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        false,
                        false)
                .parent(obtainStand)
                .addCriterion("has_arrow", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.LIVING_ARROW.get()))
                .build(JCraft.id("obtain_living_arrow"));
        consumer.accept(obtainLivingArrow);
        // obtain requiem arrow
        final Advancement obtainRequiemArrow = Advancement.Builder.advancement()
                .display(JItemRegistry.REQUIEM_ARROW.get(),
                        Component.translatable("advancements.jcraft.obtain_requiem_arrow.title"),
                        Component.translatable("advancements.jcraft.obtain_requiem_arrow.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        false,
                        false)
                .parent(obtainStand)
                .addCriterion("has_arrow", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.REQUIEM_ARROW.get()))
                .build(JCraft.id("obtain_requiem_arrow"));
        consumer.accept(obtainRequiemArrow);
        // obtain any spec
        final Advancement obtainAnySpec = Advancement.Builder.advancement()
                .display(JItemRegistry.BOXING_GLOVES.get(),
                        Component.translatable("advancements.jcraft.obtain_any_spec.title"),
                        Component.translatable("advancements.jcraft.obtain_any_spec.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(obtainMeteoriteIronOre)
                .addCriterion("has_spec", ObtainedSpecTrigger.TriggerInstance.obtainedSpec())
                .build(JCraft.id("obtain_any_spec"));
        consumer.accept(obtainAnySpec);
        // obtain stand disc
        final Advancement obtainSpecDisc = Advancement.Builder.advancement()
                .display(JItemRegistry.SPEC_DISC.get(),
                        Component.translatable("advancements.jcraft.obtain_spec_disc.title"),
                        Component.translatable("advancements.jcraft.obtain_spec_disc.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(obtainAnySpec)
                .addCriterion("has_disc", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.SPEC_DISC.get()))
                .build(JCraft.id("obtain_spec_disc"));
        consumer.accept(obtainSpecDisc);
        // find stone mask
        final Advancement findStoneMask = Advancement.Builder.advancement()
                .display(JItemRegistry.STONE_MASK.get(),
                        Component.translatable("advancements.jcraft.find_stone_mask.title"),
                        Component.translatable("advancements.jcraft.find_stone_mask.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(obtainAnySpec)
                .addCriterion("has_mask", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.STONE_MASK.get()))
                .build(JCraft.id("find_stone_mask"));
        consumer.accept(findStoneMask);
        // obtain coffin block
        final Advancement obtainCoffin = Advancement.Builder.advancement()
                .display(JItemRegistry.COFFIN_BLOCK.get(),
                        Component.translatable("advancements.jcraft.obtain_coffin.title"),
                        Component.translatable("advancements.jcraft.obtain_coffin.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .addCriterion("has_coffin", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.COFFIN_BLOCK.get()))
                .build(JCraft.id("obtain_coffin"));
        consumer.accept(obtainCoffin);
        // obtain blood bottle
        final Advancement obtainBloodBottle = Advancement.Builder.advancement()
                .display(JItemRegistry.BLOOD_BOTTLE.get(),
                        Component.translatable("advancements.jcraft.obtain_blood_bottle.title"),
                        Component.translatable("advancements.jcraft.obtain_blood_bottle.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .addCriterion("has_bottle", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.BLOOD_BOTTLE.get()))
                .build(JCraft.id("obtain_blood_bottle"));
        consumer.accept(obtainBloodBottle);
        // find stone mask
        final Advancement obtainRedHat = Advancement.Builder.advancement()
                .display(JItemRegistry.STONE_MASK.get(),
                        Component.translatable("advancements.jcraft.obtain_red_hat.title"),
                        Component.translatable("advancements.jcraft.obtain_red_hat.description"),
                        null,
                        FrameType.TASK,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .addCriterion("has_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RED_HAT.get()))
                .build(JCraft.id("obtain_red_hat"));
        consumer.accept(obtainRedHat);
        // obtain all specs
        final var obtainableSpecs = List.of(
                JSpecTypeRegistry.ANUBIS,
                JSpecTypeRegistry.BRAWLER,
                JSpecTypeRegistry.VAMPIRE,
                JSpecTypeRegistry.HAMON
        );
        final Advancement.Builder obtainAllSpecsBuilder = Advancement.Builder.advancement()
                .display(JItemRegistry.SPEC_DISC.get(),
                        Component.translatable("advancements.jcraft.obtain_all_specs.title"),
                        Component.translatable("advancements.jcraft.obtain_all_specs.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainSpecDisc)
                .rewards(AdvancementRewards.Builder.experience(1395)); // that's from level 0 to 30
        for (final RegistrySupplier<SpecType> type : obtainableSpecs) {
            obtainAllSpecsBuilder.addCriterion("has_" + type.getId().getPath(), ObtainedSpecTrigger.TriggerInstance.obtainedSpec(type.get()));
        }
        consumer.accept(obtainAllSpecsBuilder.build(JCraft.id("obtain_all_specs")));
        // obtain Diary Page
        final Advancement obtainDiaryPage = Advancement.Builder.advancement()
                .display(JItemRegistry.DIARY_PAGE.get(),
                        Component.translatable("advancements.jcraft.obtain_diary_page.title"),
                        Component.translatable("advancements.jcraft.obtain_diary_page.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        false,
                        false)
                .parent(obtainMeteoriteIronOre)
                .addCriterion("has_diary_page", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIARY_PAGE.get()))
                .rewards(AdvancementRewards.Builder.experience(100))
                .build(JCraft.id("obtain_diary_page"));
        consumer.accept(obtainDiaryPage);
        // obtain Dio's diary
        final Advancement obtainDiosDiary = Advancement.Builder.advancement()
                .display(JItemRegistry.DIOS_DIARY.get(),
                        Component.translatable("advancements.jcraft.obtain_dios_diary.title"),
                        Component.translatable("advancements.jcraft.obtain_dios_diary.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        false,
                        false)
                .parent(obtainDiaryPage)
                .addCriterion("has_diary", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIOS_DIARY.get()))
                .rewards(AdvancementRewards.Builder.experience(800))
                .build(JCraft.id("obtain_dios_diary"));
        consumer.accept(obtainDiosDiary);
        // kill dummy
        final Advancement killDummy = Advancement.Builder.advancement()
                .display(JItemRegistry.TRAINING_DUMMY.get(),
                        Component.translatable("advancements.jcraft.kill_dummy.title"),
                        Component.translatable("advancements.jcraft.kill_dummy.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        true)
                .parent(obtainMeteoriteIronOre)
                .addCriterion("killed_dummy", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(JEntityTypeRegistry.TRAINING_DUMMY.get())))
                .rewards(AdvancementRewards.Builder.experience(1000))
                .build(JCraft.id("kill_dummy"));
        consumer.accept(killDummy);
        // hamon advancements
        final ItemStack hamon = SpecDiscItem.createDiscStack(JSpecTypeRegistry.HAMON.get());
        final Advancement hamon1 = Advancement.Builder.advancement()
                .display(hamon,
                        Component.translatable("advancements.jcraft.hamon1.title"),
                        Component.translatable("advancements.jcraft.hamon1.description"),
                        null,
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .parent(obtainAnySpec)
                .addCriterion("breathed", Hamon1Trigger.TriggerInstance.trigger())
                .build(JCraft.id("hamon1"));
        consumer.accept(hamon1);
        final Advancement hamon2 = Advancement.Builder.advancement()
                .display(hamon,
                        Component.translatable("advancements.jcraft.hamon2.title"),
                        Component.translatable("advancements.jcraft.hamon2.description"),
                        null,
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .parent(hamon1)
                .addCriterion("toggled", Hamon2Trigger.TriggerInstance.trigger())
                .build(JCraft.id("hamon2"));
        consumer.accept(hamon2);
        final Advancement hamon3 = Advancement.Builder.advancement()
                .display(hamon,
                        Component.translatable("advancements.jcraft.hamon3.title"),
                        Component.translatable("advancements.jcraft.hamon3.description"),
                        null,
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .parent(hamon2)
                .addCriterion("punched", Hamon3Trigger.TriggerInstance.trigger())
                .build(JCraft.id("hamon3"));
        consumer.accept(hamon3);
        final Advancement hamon4 = Advancement.Builder.advancement()
                .display(hamon,
                        Component.translatable("advancements.jcraft.hamon4.title"),
                        Component.translatable("advancements.jcraft.hamon4.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        true,
                        false)
                .parent(hamon3)
                .addCriterion("sendoed", Hamon4Trigger.TriggerInstance.trigger())
                .build(JCraft.id("hamon4"));
        consumer.accept(hamon4);
        final Advancement hamon5 = Advancement.Builder.advancement()
                .display(hamon,
                        Component.translatable("advancements.jcraft.hamon5.title"),
                        Component.translatable("advancements.jcraft.hamon5.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        true,
                        false)
                .parent(hamon4)
                .addCriterion("waved", Hamon5Trigger.TriggerInstance.hitAtLeastEnemies(3))
                .build(JCraft.id("hamon5"));
        consumer.accept(hamon5);
        final Advancement hamon6 = Advancement.Builder.advancement()
                .display(hamon,
                        Component.translatable("advancements.jcraft.hamon6.title"),
                        Component.translatable("advancements.jcraft.hamon6.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(hamon5)
                .addCriterion("waved", Hamon6Trigger.TriggerInstance.trigger())
                .build(JCraft.id("hamon6"));
        consumer.accept(hamon6);
    }
}
