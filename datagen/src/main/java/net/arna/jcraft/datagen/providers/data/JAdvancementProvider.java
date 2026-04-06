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
import net.arna.jcraft.common.advancements.TuskAct2Trigger;
import net.arna.jcraft.common.advancements.TuskAct3Trigger;
import net.arna.jcraft.common.item.SpecDiscItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
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
        // obtain sun protections
        final Advancement obtainSunProtection = Advancement.Builder.advancement()
                .display(JItemRegistry.KARS_HEADWRAP.get(),
                        Component.translatable("advancements.jcraft.obtain_sun_protection.title"),
                        Component.translatable("advancements.jcraft.obtain_sun_protection.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        false,
                        false)
                .parent(findStoneMask)
                .addCriterion("has_kars_headwrap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KARS_HEADWRAP.get()))
                .addCriterion("has_red_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RED_HAT.get()))
                .addCriterion("has_puccis_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.PUCCIS_HAT.get()))
                .addCriterion("has_risotto_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RISOTTO_CAP.get()))
                .addCriterion("has_diego_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIEGO_HAT.get()))
                .build(JCraft.id("obtain_sun_protection"));
        consumer.accept(obtainSunProtection);
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
        // obtain any cosplay
        final Advancement obtainCosplay = Advancement.Builder.advancement()
                .display(JItemRegistry.DIO_CAPE.get(),
                        Component.translatable("advancements.jcraft.obtain_cosplay.title"),
                        Component.translatable("advancements.jcraft.obtain_cosplay.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        false,
                        false)
                .parent(obtainMeteoriteIronOre)
                .addCriterion("has_cosplay", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(JTagRegistry.COSPLAY).build()))
                .build(JCraft.id("obtain_cosplay"));
        consumer.accept(obtainCosplay);
        // obtain Dio P1 outfit
        final Advancement obtainDioP1Outfit = Advancement.Builder.advancement()
                .display(JItemRegistry.DIO_P1_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_dio_p1_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_dio_p1_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_dio_p1_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_P1_WIG.get()))
                .addCriterion("has_dio_p1_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_P1_JACKET.get()))
                .addCriterion("has_dio_p1_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_P1_PANTS.get()))
                .addCriterion("has_dio_p1_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_P1_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_dio_p1_outfit"));
        consumer.accept(obtainDioP1Outfit);
        // obtain Jotaro P3 outfit
        final Advancement obtainJotaroOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.JOTARO_CAP.get(),
                        Component.translatable("advancements.jcraft.obtain_jotaro_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_jotaro_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_jotaro_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_CAP.get()))
                .addCriterion("has_jotaro_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_JACKET.get()))
                .addCriterion("has_jotaro_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_PANTS.get()))
                .addCriterion("has_jotaro_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_jotaro_outfit"));
        consumer.accept(obtainJotaroOutfit);
        // obtain Jotaro P4 outfit
        final Advancement obtainJotaroP4Outfit = Advancement.Builder.advancement()
                .display(JItemRegistry.JOTARO_P4_CAP.get(),
                        Component.translatable("advancements.jcraft.obtain_jotaro_p4_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_jotaro_p4_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainJotaroOutfit)
                .addCriterion("has_jotaro_p4_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P4_CAP.get()))
                .addCriterion("has_jotaro_p4_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P4_JACKET.get()))
                .addCriterion("has_jotaro_p4_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P4_PANTS.get()))
                .addCriterion("has_jotaro_p4_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P4_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_jotaro_p4_outfit"));
        consumer.accept(obtainJotaroP4Outfit);
        // obtain Jotaro P6 outfit
        final Advancement obtainJotaroP6Outfit = Advancement.Builder.advancement()
                .display(JItemRegistry.JOTARO_P6_CAP.get(),
                        Component.translatable("advancements.jcraft.obtain_jotaro_p6_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_jotaro_p6_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainJotaroP4Outfit)
                .addCriterion("has_jotaro_p6_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P6_CAP.get()))
                .addCriterion("has_jotaro_p6_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P6_JACKET.get()))
                .addCriterion("has_jotaro_p6_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P6_PANTS.get()))
                .addCriterion("has_jotaro_p6_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOTARO_P6_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_jotaro_p6_outfit"));
        consumer.accept(obtainJotaroP6Outfit);
        // obtain Kakyoin outfit
        final Advancement obtainKakyoinOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.KAKYOIN_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_kakyoin_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_kakyoin_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_kakyoin_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KAKYOIN_WIG.get()))
                .addCriterion("has_kakyoin_coat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KAKYOIN_COAT.get()))
                .addCriterion("has_kakyoin_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KAKYOIN_PANTS.get()))
                .addCriterion("has_kakyoin_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KAKYOIN_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_kakyoin_outfit"));
        consumer.accept(obtainKakyoinOutfit);
        // obtain Kira outfit
        final Advancement obtainKiraOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.KIRA_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_kira_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_kira_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_kira_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KIRA_WIG.get()))
                .addCriterion("has_kira_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KIRA_JACKET.get()))
                .addCriterion("has_kira_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KIRA_PANTS.get()))
                .addCriterion("has_kira_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KIRA_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_kira_outfit"));
        consumer.accept(obtainKiraOutfit);
        // obtain Kosaku outfit
        final Advancement obtainKosakuOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.KOSAKU_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_kosaku_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_kosaku_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainKiraOutfit)
                .addCriterion("has_kosaku_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KOSAKU_WIG.get()))
                .addCriterion("has_kosaku_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KOSAKU_JACKET.get()))
                .addCriterion("has_kosaku_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KOSAKU_PANTS.get()))
                .addCriterion("has_kosaku_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.KOSAKU_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_kosaku_outfit"));
        consumer.accept(obtainKosakuOutfit);
        // obtain Final Kira outfit
        final Advancement obtainFinalKiraOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.FINAL_KIRA_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_final_kira_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_final_kira_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainKosakuOutfit)
                .addCriterion("has_final_kira_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.FINAL_KIRA_WIG.get()))
                .addCriterion("has_final_kira_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.FINAL_KIRA_JACKET.get()))
                .addCriterion("has_final_kira_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.FINAL_KIRA_PANTS.get()))
                .addCriterion("has_final_kira_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.FINAL_KIRA_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_final_kira_outfit"));
        consumer.accept(obtainFinalKiraOutfit);
        // obtain Giorno outfit
        final Advancement obtainGiornoOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.GIORNO_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_giorno_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_giorno_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_giorno_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GIORNO_WIG.get()))
                .addCriterion("has_giorno_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GIORNO_JACKET.get()))
                .addCriterion("has_giorno_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GIORNO_PANTS.get()))
                .addCriterion("has_giorno_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GIORNO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_giorno_outfit"));
        consumer.accept(obtainGiornoOutfit);
        // obtain Risotto outfit
        final Advancement obtainRisottoOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.RISOTTO_CAP.get(),
                        Component.translatable("advancements.jcraft.obtain_risotto_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_risotto_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_risotto_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RISOTTO_CAP.get()))
                .addCriterion("has_risotto_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RISOTTO_JACKET.get()))
                .addCriterion("has_risotto_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RISOTTO_PANTS.get()))
                .addCriterion("has_risotto_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RISOTTO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_risotto_outfit"));
        consumer.accept(obtainRisottoOutfit);
        // obtain Doppio outfit
        final Advancement obtainDoppioOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.DOPPIO_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_doppio_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_doppio_outfit.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        false,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_doppio_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DOPPIO_WIG.get()))
                .addCriterion("has_doppio_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DOPPIO_SHIRT.get()))
                .build(JCraft.id("obtain_doppio_outfit"));
        consumer.accept(obtainDoppioOutfit);
        // obtain Diavolo outfit
        final Advancement obtainDiavoloOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.DIAVOLO_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_diavolo_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_diavolo_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainDoppioOutfit)
                .addCriterion("has_diavolo_wig", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIAVOLO_WIG.get()))
                .addCriterion("has_diavolo_shirt", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIAVOLO_SHIRT.get()))
                .addCriterion("has_diavolo_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIAVOLO_PANTS.get()))
                .addCriterion("has_diavolo_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIAVOLO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_diavolo_outfit"));
        consumer.accept(obtainDiavoloOutfit);
        // obtain Johnny outfit
        final Advancement obtainJohnnyOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.JOHNNY_CAP.get(),
                        Component.translatable("advancements.jcraft.obtain_johnny_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_johnny_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_johnny_cap", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOHNNY_CAP.get()))
                .addCriterion("has_johnny_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOHNNY_JACKET.get()))
                .addCriterion("has_johnny_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOHNNY_PANTS.get()))
                .addCriterion("has_johnny_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.JOHNNY_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_johnny_outfit"));
        consumer.accept(obtainJohnnyOutfit);
        // obtain Gyro outfit
        final Advancement obtainGyroOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.GYRO_HAT.get(),
                        Component.translatable("advancements.jcraft.obtain_gyro_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_gyro_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_gyro_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GYRO_HAT.get()))
                .addCriterion("has_gyro_shirt", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GYRO_SHIRT.get()))
                .addCriterion("has_gyro_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GYRO_PANTS.get()))
                .addCriterion("has_gyro_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.GYRO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_gyro_outfit"));
        consumer.accept(obtainGyroOutfit);
        // obtain Diego outfit
        final Advancement obtainDiegoOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.DIEGO_HAT.get(),
                        Component.translatable("advancements.jcraft.obtain_diego_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_diego_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_diego_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIEGO_HAT.get()))
                .addCriterion("has_diego_shirt", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIEGO_SHIRT.get()))
                .addCriterion("has_diego_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIEGO_PANTS.get()))
                .addCriterion("has_diego_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIEGO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_diego_outfit"));
        consumer.accept(obtainDiegoOutfit);
        // obtain Ringo outfit
        final Advancement obtainRingoOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.RINGO_OUTFIT.get(),
                        Component.translatable("advancements.jcraft.obtain_ringo_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_ringo_outfit.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        false,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_ringo_outfit", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RINGO_OUTFIT.get()))
                .addCriterion("has_ringo_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.RINGO_BOOTS.get()))
                .build(JCraft.id("obtain_ringo_outfit"));
        consumer.accept(obtainRingoOutfit);
        // obtain Valentine outfit
        final Advancement obtainValentineOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.VALENTINE_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_valentine_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_valentine_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_valentine_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.VALENTINE_WIG.get()))
                .addCriterion("has_valentine_shirt", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.VALENTINE_JACKET.get()))
                .addCriterion("has_valentine_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.VALENTINE_PANTS.get()))
                .addCriterion("has_valentine_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.VALENTINE_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_valentine_outfit"));
        consumer.accept(obtainValentineOutfit);
        // obtain Pucci outfit
        final Advancement obtainPucciOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.PUCCIS_HAT.get(),
                        Component.translatable("advancements.jcraft.obtain_pucci_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_pucci_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainCosplay)
                .addCriterion("has_pucci_hat", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.PUCCIS_HAT.get()))
                .addCriterion("has_pucci_robe", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.PUCCI_ROBE.get()))
                .addCriterion("has_pucci_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.PUCCI_PANTS.get()))
                .addCriterion("has_pucci_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.PUCCI_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_pucci_outfit"));
        consumer.accept(obtainPucciOutfit);
        // obtain Dio outfit
        final Advancement obtainDioOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.DIO_HEADBAND.get(),
                        Component.translatable("advancements.jcraft.obtain_dio_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_dio_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainDioP1Outfit)
                .addCriterion("has_dio_headband", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_HEADBAND.get()))
                .addCriterion("has_dio_jacket", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_JACKET.get()))
                .addCriterion("has_dio_cape", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_CAPE.get()))
                .addCriterion("has_dio_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_PANTS.get()))
                .addCriterion("has_dio_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.DIO_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .rewards(AdvancementRewards.Builder.recipe(JCraft.id("dios_diary")))
                .build(JCraft.id("obtain_dio_outfit"));
        consumer.accept(obtainDioOutfit);
        // obtain Heaven Attained outfit
        final Advancement obtainHeavenAttainedOutfit = Advancement.Builder.advancement()
                .display(JItemRegistry.HEAVEN_ATTAINED_WIG.get(),
                        Component.translatable("advancements.jcraft.obtain_heaven_attained_outfit.title"),
                        Component.translatable("advancements.jcraft.obtain_heaven_attained_outfit.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(obtainDioOutfit)
                .addCriterion("has_heaven_attained_headband", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.HEAVEN_ATTAINED_WIG.get()))
                .addCriterion("has_heaven_attained_shirt", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.HEAVEN_ATTAINED_SHIRT.get()))
                .addCriterion("has_heaven_attained_pants", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.HEAVEN_ATTAINED_PANTS.get()))
                .addCriterion("has_heaven_attained_boots", InventoryChangeTrigger.TriggerInstance.hasItems(JItemRegistry.HEAVEN_ATTAINED_BOOTS.get()))
                .rewards(AdvancementRewards.Builder.experience(200))
                .build(JCraft.id("obtain_heaven_attained_outfit"));
        consumer.accept(obtainHeavenAttainedOutfit);
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
        // Tusk Act 1
        final Advancement tuskAct1 = Advancement.Builder.advancement()
                .display(JItemRegistry.LEFT_ARM.get(),
                        Component.translatable("advancements.jcraft.tusk_act1.title"),
                        Component.translatable("advancements.jcraft.tusk_act1.description"),
                        null,
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .parent(obtainStand)
                .addCriterion("obtained", ObtainedStandTrigger.TriggerInstance.obtainedStand(JStandTypeRegistry.TUSK_ACT_1.get()))
                .build(JCraft.id("tusk_act1"));
        consumer.accept(tuskAct1);
        // Tusk Act 2
        final Advancement tuskAct2 = Advancement.Builder.advancement()
                .display(JItemRegistry.LEFT_ARM.get(),
                        Component.translatable("advancements.jcraft.tusk_act2.title"),
                        Component.translatable("advancements.jcraft.tusk_act2.description"),
                        null,
                        FrameType.GOAL,
                        true,
                        true,
                        false)
                .parent(tuskAct1)
                .addCriterion("unlocked", TuskAct2Trigger.TriggerInstance.trigger())
                .build(JCraft.id("tusk_act2"));
        consumer.accept(tuskAct2);
        // Tusk Act 3
        final Advancement tuskAct3 = Advancement.Builder.advancement()
                .display(JItemRegistry.LEFT_ARM.get(),
                        Component.translatable("advancements.jcraft.tusk_act3.title"),
                        Component.translatable("advancements.jcraft.tusk_act3.description"),
                        null,
                        FrameType.CHALLENGE,
                        true,
                        true,
                        false)
                .parent(tuskAct2)
                .addCriterion("unlocked", TuskAct3Trigger.TriggerInstance.trigger())
                .build(JCraft.id("tusk_act3"));
        consumer.accept(tuskAct3);
    }
}
