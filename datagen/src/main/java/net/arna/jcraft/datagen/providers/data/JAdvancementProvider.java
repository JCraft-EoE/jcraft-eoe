package net.arna.jcraft.datagen.providers.data;

import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
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
import net.arna.jcraft.common.item.CosplayItem;
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
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

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
        final Advancement obtainSunProtection = generateCosplayAdvancement(
                "sun_protection",
                JItemRegistry.KARS_HEADWRAP.get(ArmorMaterials.IRON).get(),
                FrameType.GOAL,
                findStoneMask,
                JItemRegistry.KARS_HEADWRAP,
                JItemRegistry.RED_HAT,
                JItemRegistry.PUCCIS_HAT,
                JItemRegistry.RISOTTO_CAP,
                JItemRegistry.DIEGO_HAT,
                JItemRegistry.COWBOY_HAT,
                JItemRegistry.MOUNTAIN_TIM_HAT);
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
                .display(JItemRegistry.DIO_CAPE.get(ArmorMaterials.NETHERITE).get(),
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
        final Advancement obtainDioP1Outfit = generateCosplayAdvancement(
                "dio_p1_outfit",
                JItemRegistry.DIO_P1_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.DIO_P1_WIG,
                JItemRegistry.DIO_P1_JACKET,
                JItemRegistry.DIO_P1_PANTS,
                JItemRegistry.DIO_P1_BOOTS
        );
        consumer.accept(obtainDioP1Outfit);
        // obtain Jotaro P3 outfit
        final Advancement obtainJotaroOutfit = generateCosplayAdvancement(
                "jotaro_outfit",
                JItemRegistry.JOTARO_CAP.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.JOTARO_CAP,
                JItemRegistry.JOTARO_JACKET,
                JItemRegistry.JOTARO_PANTS,
                JItemRegistry.JOTARO_BOOTS
        );
        consumer.accept(obtainJotaroOutfit);
        // obtain Jotaro P4 outfit
        final Advancement obtainJotaroP4Outfit = generateCosplayAdvancement(
                "jotaro_p4_outfit",
                JItemRegistry.JOTARO_P4_CAP.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainJotaroOutfit,
                JItemRegistry.JOTARO_P4_CAP,
                JItemRegistry.JOTARO_P4_JACKET,
                JItemRegistry.JOTARO_P4_PANTS,
                JItemRegistry.JOTARO_P4_BOOTS
        );
        consumer.accept(obtainJotaroP4Outfit);
        // obtain Jotaro P6 outfit
        final Advancement obtainJotaroP6Outfit = generateCosplayAdvancement(
                "jotaro_p6_outfit",
                JItemRegistry.JOTARO_P6_CAP.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainJotaroP4Outfit,
                JItemRegistry.JOTARO_P6_CAP,
                JItemRegistry.JOTARO_P6_JACKET,
                JItemRegistry.JOTARO_P6_PANTS,
                JItemRegistry.JOTARO_P6_BOOTS
        );
        consumer.accept(obtainJotaroP6Outfit);
        // obtain Kakyoin outfit
        final Advancement obtainKakyoinOutfit = generateCosplayAdvancement(
                "kakyoin_outfit",
                JItemRegistry.KAKYOIN_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.KAKYOIN_WIG,
                JItemRegistry.KAKYOIN_COAT,
                JItemRegistry.KAKYOIN_PANTS,
                JItemRegistry.KAKYOIN_BOOTS
        );
        consumer.accept(obtainKakyoinOutfit);
        // obtain Polnareff outfit
        final Advancement obtainPolnareffOutfit = generateCosplayAdvancement(
                "polnareff_outfit",
                JItemRegistry.POLNAREFF_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.POLNAREFF_WIG,
                JItemRegistry.POLNAREFF_SHIRT,
                JItemRegistry.POLNAREFF_PANTS,
                JItemRegistry.POLNAREFF_BOOTS
        );
        consumer.accept(obtainPolnareffOutfit);
        // obtain Kira outfit
        final Advancement obtainKiraOutfit = generateCosplayAdvancement(
                "kira_outfit",
                JItemRegistry.KIRA_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.KIRA_WIG,
                JItemRegistry.KIRA_JACKET,
                JItemRegistry.KIRA_PANTS,
                JItemRegistry.KIRA_BOOTS
        );
        consumer.accept(obtainKiraOutfit);
        // obtain Kosaku outfit
        final Advancement obtainKosakuOutfit = generateCosplayAdvancement(
                "kosaku_outfit",
                JItemRegistry.KOSAKU_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainKiraOutfit,
                JItemRegistry.KOSAKU_WIG,
                JItemRegistry.KOSAKU_JACKET,
                JItemRegistry.KOSAKU_PANTS,
                JItemRegistry.KOSAKU_BOOTS
        );
        consumer.accept(obtainKosakuOutfit);
        // obtain Final Kira outfit
        final Advancement obtainFinalKiraOutfit = generateCosplayAdvancement(
                "final_kira_outfit",
                JItemRegistry.FINAL_KIRA_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainKiraOutfit,
                JItemRegistry.FINAL_KIRA_WIG,
                JItemRegistry.FINAL_KIRA_JACKET,
                JItemRegistry.FINAL_KIRA_PANTS,
                JItemRegistry.FINAL_KIRA_BOOTS
        );
        consumer.accept(obtainFinalKiraOutfit);
        // obtain Giorno outfit
        final Advancement obtainGiornoOutfit = generateCosplayAdvancement(
                "giorno_outfit",
                JItemRegistry.GIORNO_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.GIORNO_WIG,
                JItemRegistry.GIORNO_JACKET,
                JItemRegistry.GIORNO_PANTS,
                JItemRegistry.GIORNO_BOOTS
        );
        consumer.accept(obtainGiornoOutfit);
        // obtain Risotto outfit
        final Advancement obtainRisottoOutfit = generateCosplayAdvancement(
                "risotto_outfit",
                JItemRegistry.RISOTTO_CAP.get(ArmorMaterials.IRON).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.RISOTTO_CAP,
                JItemRegistry.RISOTTO_JACKET,
                JItemRegistry.RISOTTO_PANTS,
                JItemRegistry.RISOTTO_BOOTS
        );
        consumer.accept(obtainRisottoOutfit);
        // obtain Doppio outfit
        final Advancement obtainDoppioOutfit = generateCosplayAdvancement(
                "doppio_outfit",
                JItemRegistry.DOPPIO_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.GOAL,
                obtainCosplay,
                JItemRegistry.DOPPIO_WIG,
                JItemRegistry.DOPPIO_SHIRT
        );
        consumer.accept(obtainDoppioOutfit);
        // obtain Diavolo outfit
        final Advancement obtainDiavoloOutfit = generateCosplayAdvancement(
                "diavolo_outfit",
                JItemRegistry.DIAVOLO_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainDoppioOutfit,
                JItemRegistry.DIAVOLO_WIG,
                JItemRegistry.DIAVOLO_SHIRT,
                JItemRegistry.DIAVOLO_PANTS,
                JItemRegistry.DIAVOLO_BOOTS
        );
        consumer.accept(obtainDiavoloOutfit);
        // obtain Johnny outfit
        final Advancement obtainJohnnyOutfit = generateCosplayAdvancement(
                "johnny_outfit",
                JItemRegistry.JOHNNY_CAP.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.JOHNNY_CAP,
                JItemRegistry.JOHNNY_JACKET,
                JItemRegistry.JOHNNY_PANTS,
                JItemRegistry.JOHNNY_BOOTS
        );
        consumer.accept(obtainJohnnyOutfit);
        // obtain Gyro outfit
        final Advancement obtainGyroOutfit = generateCosplayAdvancement(
                "gyro_outfit",
                JItemRegistry.GYRO_HAT.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.GYRO_HAT,
                JItemRegistry.GYRO_SHIRT,
                JItemRegistry.GYRO_PANTS,
                JItemRegistry.GYRO_BOOTS
        );
        consumer.accept(obtainGyroOutfit);
        // obtain Cowboy outfit
        final Advancement obtainCowboyOutfit = generateCosplayAdvancement(
                "cowboy_outfit",
                JItemRegistry.COWBOY_HAT.get(ArmorMaterials.IRON).get(),
                FrameType.GOAL,
                obtainCosplay,
                JItemRegistry.COWBOY_HAT,
                JItemRegistry.COWBOY_PONCHO,
                JItemRegistry.COWBOY_GUNBELT_SPURS
        );
        consumer.accept(obtainCowboyOutfit);
        // obtain Diego outfit
        final Advancement obtainDiegoOutfit = generateCosplayAdvancement(
                "diego_outfit",
                JItemRegistry.DIEGO_HAT.get(ArmorMaterials.IRON).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.DIEGO_HAT,
                JItemRegistry.DIEGO_SHIRT,
                JItemRegistry.DIEGO_PANTS,
                JItemRegistry.DIEGO_BOOTS
        );
        consumer.accept(obtainDiegoOutfit);
        // obtain Ringo outfit
        final Advancement obtainRingoOutfit = generateCosplayAdvancement(
                "ringo_outfit",
                JItemRegistry.RINGO_OUTFIT.get(ArmorMaterials.NETHERITE).get(),
                FrameType.GOAL,
                obtainCosplay,
                JItemRegistry.RINGO_OUTFIT,
                JItemRegistry.RINGO_BOOTS
        );
        consumer.accept(obtainRingoOutfit);
        // obtain Valentine outfit
        final Advancement obtainValentineOutfit = generateCosplayAdvancement(
                "valentine_outfit",
                JItemRegistry.VALENTINE_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.VALENTINE_WIG,
                JItemRegistry.VALENTINE_JACKET,
                JItemRegistry.VALENTINE_PANTS,
                JItemRegistry.VALENTINE_BOOTS
        );
        consumer.accept(obtainValentineOutfit);
        // obtain Pucci outfit
        final Advancement obtainPucciOutfit = generateCosplayAdvancement(
                "pucci_outfit",
                JItemRegistry.PUCCIS_HAT.get(ArmorMaterials.IRON).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.PUCCIS_HAT,
                JItemRegistry.PUCCI_ROBE,
                JItemRegistry.PUCCI_PANTS,
                JItemRegistry.PUCCI_BOOTS
        );
        consumer.accept(obtainPucciOutfit);
        // obtain DIO outfit
        final Advancement obtainDioOutfit = generateCosplayAdvancement(
                "dio_outfit",
                JItemRegistry.DIO_HEADBAND.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainDioP1Outfit,
                JItemRegistry.DIO_HEADBAND,
                JItemRegistry.DIO_JACKET,
                JItemRegistry.DIO_CAPE,
                JItemRegistry.DIO_PANTS,
                JItemRegistry.DIO_BOOTS
        );
        consumer.accept(obtainDioOutfit);
        // obtain OH DIO outfit
        final Advancement obtainOhDioOutfit = generateCosplayAdvancement(
                "oh_dio_outfit",
                JItemRegistry.OH_DIO_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainDioOutfit,
                JItemRegistry.OH_DIO_WIG,
                JItemRegistry.OH_DIO_JACKET,
                JItemRegistry.OH_DIO_PANTS,
                JItemRegistry.OH_DIO_BOOTS
        );
        consumer.accept(obtainOhDioOutfit);
        // obtain Heaven Attained outfit
        final Advancement obtainHeavenAttainedOutfit = generateCosplayAdvancement(
                "heaven_attained_outfit",
                JItemRegistry.HEAVEN_ATTAINED_WIG.get(ArmorMaterials.NETHERITE).get(),
                FrameType.CHALLENGE,
                obtainOhDioOutfit,
                JItemRegistry.HEAVEN_ATTAINED_WIG,
                JItemRegistry.HEAVEN_ATTAINED_SHIRT,
                JItemRegistry.HEAVEN_ATTAINED_PANTS,
                JItemRegistry.HEAVEN_ATTAINED_BOOTS
        );
        consumer.accept(obtainHeavenAttainedOutfit);
        // obtain DIO outfit
        final Advancement obtainMountainTimOutfit = generateCosplayAdvancement(
                "mountain_tim_outfit",
                JItemRegistry.MOUNTAIN_TIM_HAT.get(ArmorMaterials.IRON).get(),
                FrameType.CHALLENGE,
                obtainCosplay,
                JItemRegistry.MOUNTAIN_TIM_HAT,
                JItemRegistry.MOUNTAIN_TIM_SHIRT,
                JItemRegistry.MOUNTAIN_TIM_COAT,
                JItemRegistry.MOUNTAIN_TIM_PANTS,
                JItemRegistry.MOUNTAIN_TIM_BOOTS
        );
        consumer.accept(obtainMountainTimOutfit);
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

    protected Advancement generateCosplayAdvancement(final @NonNull String name, final @NonNull Item display, final @NonNull FrameType frame, final @NonNull Advancement parent, final @NonNull CosplayItem<?>... pieces) {
        if (pieces.length == 0) {
            throw new IllegalArgumentException("At least one cosplay piece must be specified!");
        }
        final String keyBase = "advancements." + JCraft.MOD_ID + ".obtain_" + name;
        var builder = Advancement.Builder.advancement()
                .display(display,
                        Component.translatable(keyBase + ".title"),
                        Component.translatable(keyBase + ".description"),
                        null,
                        frame,
                        true,
                        true,
                        false)
                .parent(parent);
        if (frame == FrameType.CHALLENGE) {
            builder = builder.rewards(AdvancementRewards.Builder.experience(200));
        }
        for (final CosplayItem<?> piece : pieces) {
            builder.addCriterion("has_" + piece.getName(),
                    InventoryChangeTrigger.TriggerInstance.hasItems(
                            ItemPredicate.Builder.item().of(piece.getTag()).build()));
        }
        return builder.build(JCraft.id("obtain_" + name));
    }
}
