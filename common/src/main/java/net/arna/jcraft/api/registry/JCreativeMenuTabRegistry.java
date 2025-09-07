package net.arna.jcraft.api.registry;

import dev.architectury.registry.CreativeTabRegistry;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.api.spec.SpecTypeUtil;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.item.SpecDiscItem;
import net.arna.jcraft.common.item.StandDiscItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public interface JCreativeMenuTabRegistry {
    @SuppressWarnings("UnstableApiUsage") // we do not care :)
    static void init() {
        JCraft.CREATIVE_TAB_REGISTRY.register("general", JCreativeMenuTabRegistry::createJcraftItemGroup);
        JCraft.CREATIVE_TAB_REGISTRY.register("cosplay", JCreativeMenuTabRegistry::createJcraftCosplayGroup);
        JCraft.CREATIVE_TAB_REGISTRY.register("stand_discs", JCreativeMenuTabRegistry::createStandDiscItemGroup);
        // building blocks
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.BUILDING_BLOCKS.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.BRICKS, JItemRegistry.METEORITE_BLOCK.get());
            output.acceptBefore(Items.BRICKS, JItemRegistry.POLISHED_METEORITE_BLOCK.get());
            output.acceptBefore(Items.GOLD_BLOCK, JItemRegistry.STELLAR_IRON_BLOCK.get());
            output.acceptBefore(Items.SEA_LANTERN, JItemRegistry.CINDERELLA_GREEN_BLOCK.get());
            output.acceptBefore(Items.CRIMSON_STEM, JItemRegistry.SOUL_WOOD_BLOCK.get());
        });
        // natural blocks
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.NATURAL_BLOCKS.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptAfter(Items.SANDSTONE, JItemRegistry.FOOLISH_SAND_BLOCK.get());
            output.acceptAfter(JItemRegistry.FOOLISH_SAND_BLOCK.get(), JItemRegistry.HOT_SAND_BLOCK.get());
            output.acceptBefore(Items.MUSHROOM_STEM, JItemRegistry.SOUL_WOOD_BLOCK.get());
            output.acceptBefore(Items.NETHER_GOLD_ORE, JItemRegistry.METEORITE_IRON_ORE_BLOCK.get());
            output.acceptBefore(Items.OBSIDIAN, JItemRegistry.METEORITE_BLOCK.get());
            output.acceptAfter(Items.SOUL_SOIL, JItemRegistry.SOUL_BLOCK.get());
        });
        // functional blocks
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FUNCTIONAL_BLOCKS.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.CANDLE, JItemRegistry.COFFIN_BLOCK.get());
        });
        // tools
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.COMPASS, JItemRegistry.STAND_ARROW.get());
            output.acceptBefore(Items.COMPASS, JItemRegistry.STAND_DISC.get());
            output.acceptBefore(Items.COMPASS, JItemRegistry.SPEC_DISC.get());
        });
        // combat
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.COMBAT.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.WOODEN_SWORD, JItemRegistry.SHIV.get());
            output.acceptBefore(Items.WOODEN_AXE, JItemRegistry.ANUBIS_SHEATHED.get());
            output.acceptBefore(Items.BOW, JItemRegistry.KNIFE.get());
            output.acceptBefore(Items.BOW, JItemRegistry.KNIFEBUNDLE.get());
            output.acceptBefore(Items.BOW, JItemRegistry.FV_REVOLVER.get());
            output.acceptBefore(Items.BOW, JItemRegistry.BULLET.get());
            output.acceptAfter(Items.NETHERITE_BOOTS, JItemRegistry.DIO_P1_WIG.get());
            output.acceptAfter(JItemRegistry.DIO_P1_WIG.get(), JItemRegistry.DIO_P1_JACKET.get());
            output.acceptAfter(JItemRegistry.DIO_P1_JACKET.get(), JItemRegistry.DIO_P1_PANTS.get());
            output.acceptAfter(JItemRegistry.DIO_P1_PANTS.get(), JItemRegistry.DIO_P1_BOOTS.get());
            output.acceptAfter(JItemRegistry.DIO_P1_BOOTS.get(), JItemRegistry.STRAIZO_PONCHO.get());
            output.acceptAfter(JItemRegistry.STRAIZO_PONCHO.get(), JItemRegistry.JOTARO_CAP.get());
            output.acceptAfter(JItemRegistry.JOTARO_CAP.get(), JItemRegistry.JOTARO_JACKET.get());
            output.acceptAfter(JItemRegistry.JOTARO_JACKET.get(), JItemRegistry.JOTARO_PANTS.get());
            output.acceptAfter(JItemRegistry.JOTARO_PANTS.get(), JItemRegistry.JOTARO_BOOTS.get());
            output.acceptAfter(JItemRegistry.JOTARO_BOOTS.get(), JItemRegistry.DIO_HEADBAND.get());
            output.acceptAfter(JItemRegistry.DIO_HEADBAND.get(), JItemRegistry.DIO_JACKET.get());
            output.acceptAfter(JItemRegistry.DIO_JACKET.get(), JItemRegistry.DIO_CAPE.get());
            output.acceptAfter(JItemRegistry.DIO_CAPE.get(), JItemRegistry.DIO_PANTS.get());
            output.acceptAfter(JItemRegistry.DIO_PANTS.get(), JItemRegistry.DIO_BOOTS.get());
            output.acceptAfter(JItemRegistry.DIO_BOOTS.get(), JItemRegistry.KAKYOIN_WIG.get());
            output.acceptAfter(JItemRegistry.KAKYOIN_WIG.get(), JItemRegistry.KAKYOIN_COAT.get());
            output.acceptAfter(JItemRegistry.KAKYOIN_COAT.get(), JItemRegistry.KAKYOIN_PANTS.get());
            output.acceptAfter(JItemRegistry.KAKYOIN_PANTS.get(), JItemRegistry.KAKYOIN_BOOTS.get());
            output.acceptAfter(JItemRegistry.KAKYOIN_BOOTS.get(), JItemRegistry.HEAVEN_ATTAINED_WIG.get());
            output.acceptAfter(JItemRegistry.HEAVEN_ATTAINED_WIG.get(), JItemRegistry.HEAVEN_ATTAINED_SHIRT.get());
            output.acceptAfter(JItemRegistry.HEAVEN_ATTAINED_SHIRT.get(), JItemRegistry.HEAVEN_ATTAINED_PANTS.get());
            output.acceptAfter(JItemRegistry.HEAVEN_ATTAINED_PANTS.get(), JItemRegistry.HEAVEN_ATTAINED_BOOTS.get());
            output.acceptAfter(JItemRegistry.HEAVEN_ATTAINED_BOOTS.get(), JItemRegistry.JOTARO_P4_CAP.get());
            output.acceptAfter(JItemRegistry.JOTARO_P4_CAP.get(), JItemRegistry.JOTARO_P4_JACKET.get());
            output.acceptAfter(JItemRegistry.JOTARO_P4_JACKET.get(), JItemRegistry.JOTARO_P4_PANTS.get());
            output.acceptAfter(JItemRegistry.JOTARO_P4_PANTS.get(), JItemRegistry.JOTARO_P4_BOOTS.get());
            output.acceptAfter(JItemRegistry.JOTARO_P4_BOOTS.get(), JItemRegistry.KIRA_WIG.get());
            output.acceptAfter(JItemRegistry.KIRA_WIG.get(), JItemRegistry.KIRA_JACKET.get());
            output.acceptAfter(JItemRegistry.KIRA_JACKET.get(), JItemRegistry.KIRA_PANTS.get());
            output.acceptAfter(JItemRegistry.KIRA_PANTS.get(), JItemRegistry.KIRA_BOOTS.get());
            output.acceptAfter(JItemRegistry.KIRA_BOOTS.get(), JItemRegistry.KOSAKU_WIG.get());
            output.acceptAfter(JItemRegistry.KOSAKU_WIG.get(), JItemRegistry.KOSAKU_JACKET.get());
            output.acceptAfter(JItemRegistry.KOSAKU_JACKET.get(), JItemRegistry.KOSAKU_PANTS.get());
            output.acceptAfter(JItemRegistry.KOSAKU_PANTS.get(), JItemRegistry.KOSAKU_BOOTS.get());
            output.acceptAfter(JItemRegistry.KOSAKU_BOOTS.get(), JItemRegistry.FINAL_KIRA_WIG.get());
            output.acceptAfter(JItemRegistry.FINAL_KIRA_WIG.get(), JItemRegistry.FINAL_KIRA_JACKET.get());
            output.acceptAfter(JItemRegistry.FINAL_KIRA_JACKET.get(), JItemRegistry.FINAL_KIRA_PANTS.get());
            output.acceptAfter(JItemRegistry.FINAL_KIRA_PANTS.get(), JItemRegistry.FINAL_KIRA_BOOTS.get());
            output.acceptAfter(JItemRegistry.FINAL_KIRA_BOOTS.get(), JItemRegistry.GIORNO_WIG.get());
            output.acceptAfter(JItemRegistry.GIORNO_WIG.get(), JItemRegistry.GIORNO_JACKET.get());
            output.acceptAfter(JItemRegistry.GIORNO_JACKET.get(), JItemRegistry.GIORNO_PANTS.get());
            output.acceptAfter(JItemRegistry.GIORNO_PANTS.get(), JItemRegistry.GIORNO_BOOTS.get());
            output.acceptAfter(JItemRegistry.GIORNO_BOOTS.get(), JItemRegistry.RISOTTO_CAP.get());
            output.acceptAfter(JItemRegistry.RISOTTO_CAP.get(), JItemRegistry.RISOTTO_JACKET.get());
            output.acceptAfter(JItemRegistry.RISOTTO_JACKET.get(), JItemRegistry.RISOTTO_PANTS.get());
            output.acceptAfter(JItemRegistry.RISOTTO_PANTS.get(), JItemRegistry.RISOTTO_BOOTS.get());
            output.acceptAfter(JItemRegistry.RISOTTO_BOOTS.get(), JItemRegistry.DOPPIO_WIG.get());
            output.acceptAfter(JItemRegistry.DOPPIO_WIG.get(), JItemRegistry.DOPPIO_SHIRT.get());
            output.acceptAfter(JItemRegistry.DOPPIO_SHIRT.get(), JItemRegistry.DIAVOLO_WIG.get());
            output.acceptAfter(JItemRegistry.DIAVOLO_WIG.get(), JItemRegistry.DIAVOLO_SHIRT.get());
            output.acceptAfter(JItemRegistry.DIAVOLO_SHIRT.get(), JItemRegistry.DIAVOLO_PANTS.get());
            output.acceptAfter(JItemRegistry.DIAVOLO_PANTS.get(), JItemRegistry.DIAVOLO_BOOTS.get());
            output.acceptAfter(JItemRegistry.DIAVOLO_BOOTS.get(), JItemRegistry.PUCCIS_HAT.get());
            output.acceptAfter(JItemRegistry.PUCCIS_HAT.get(), JItemRegistry.PUCCI_ROBE.get());
            output.acceptAfter(JItemRegistry.PUCCI_ROBE.get(), JItemRegistry.PUCCI_PANTS.get());
            output.acceptAfter(JItemRegistry.PUCCI_PANTS.get(), JItemRegistry.PUCCI_BOOTS.get());
            output.acceptAfter(JItemRegistry.PUCCI_BOOTS.get(), JItemRegistry.JOHNNY_CAP.get());
            output.acceptAfter(JItemRegistry.JOHNNY_CAP.get(), JItemRegistry.JOHNNY_JACKET.get());
            output.acceptAfter(JItemRegistry.JOHNNY_JACKET.get(), JItemRegistry.JOHNNY_PANTS.get());
            output.acceptAfter(JItemRegistry.JOHNNY_PANTS.get(), JItemRegistry.JOHNNY_BOOTS.get());
            output.acceptAfter(JItemRegistry.JOHNNY_BOOTS.get(), JItemRegistry.GYRO_HAT.get());
            output.acceptAfter(JItemRegistry.GYRO_HAT.get(), JItemRegistry.GYRO_SHIRT.get());
            output.acceptAfter(JItemRegistry.GYRO_SHIRT.get(), JItemRegistry.GYRO_PANTS.get());
            output.acceptAfter(JItemRegistry.GYRO_PANTS.get(), JItemRegistry.GYRO_BOOTS.get());
            output.acceptAfter(JItemRegistry.GYRO_BOOTS.get(), JItemRegistry.DIEGO_HAT.get());
            output.acceptAfter(JItemRegistry.DIEGO_HAT.get(), JItemRegistry.DIEGO_SHIRT.get());
            output.acceptAfter(JItemRegistry.DIEGO_SHIRT.get(), JItemRegistry.DIEGO_PANTS.get());
            output.acceptAfter(JItemRegistry.DIEGO_PANTS.get(), JItemRegistry.DIEGO_BOOTS.get());
            output.acceptAfter(JItemRegistry.DIEGO_BOOTS.get(), JItemRegistry.RINGO_OUTFIT.get());
            output.acceptAfter(JItemRegistry.RINGO_OUTFIT.get(), JItemRegistry.RINGO_BOOTS.get());
            output.acceptAfter(JItemRegistry.RINGO_BOOTS.get(), JItemRegistry.VALENTINE_WIG.get());
            output.acceptAfter(JItemRegistry.VALENTINE_WIG.get(), JItemRegistry.VALENTINE_JACKET.get());
            output.acceptAfter(JItemRegistry.VALENTINE_JACKET.get(), JItemRegistry.VALENTINE_PANTS.get());
            output.acceptAfter(JItemRegistry.VALENTINE_PANTS.get(), JItemRegistry.VALENTINE_BOOTS.get());
            output.acceptBefore(Items.SHIELD, JItemRegistry.BOXING_GLOVES.get());
            output.acceptBefore(Items.SHIELD, JItemRegistry.STEEL_BALL.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.STONE_MASK.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.RED_HAT.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.KARS_HEADWRAP.get());
        });
        // ingredients
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.INGREDIENTS.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.COPPER_INGOT, JItemRegistry.STELLAR_IRON_INGOT.get());
            output.acceptBefore(Items.GLASS_BOTTLE, JItemRegistry.LIVING_ARROW.get());
            output.acceptBefore(Items.GLASS_BOTTLE, JItemRegistry.REQUIEM_RUBY.get());
            output.acceptBefore(Items.GLASS_BOTTLE, JItemRegistry.REQUIEM_ARROW.get());
            output.acceptBefore(Items.GLASS_BOTTLE, JItemRegistry.GREEN_BABY.get());
            output.acceptBefore(Items.GLASS_BOTTLE, JItemRegistry.DIARY_PAGE.get());
            output.acceptBefore(Items.GLASS_BOTTLE, JItemRegistry.DIOS_DIARY.get());
            output.acceptAfter(Items.GLASS_BOTTLE, JItemRegistry.BLOOD_BOTTLE.get());
            output.acceptAfter(Items.DISC_FRAGMENT_5, JItemRegistry.DISC.get());
            output.acceptBefore(Items.WHITE_DYE, JItemRegistry.SINNERS_SOUL.get());
            output.acceptBefore(Items.WHITE_DYE, JItemRegistry.STAND_ARROWHEAD.get());
            output.acceptBefore(Items.WHITE_DYE, JItemRegistry.PRISON_KEY.get());
            output.acceptBefore(Items.WHITE_DYE, JItemRegistry.CINDERELLA_MASK.get());
            output.acceptBefore(Items.BOWL, JItemRegistry.KQ_COIN.get());
        });
        // foods & drinks
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FOOD_AND_DRINKS.location()), (flags, output, canUseGameMasterBlocks) -> {
            final ItemStack bloodBottle = new ItemStack(JItemRegistry.BLOOD_BOTTLE.get());
            bloodBottle.getOrCreateTag().putFloat("Blood", 16f);
            output.acceptBefore(Items.HONEY_BOTTLE, bloodBottle);
            output.acceptBefore(Items.HONEY_BOTTLE, JItemRegistry.PLANKTON_VIAL.get());
        });
        // spawn eggs
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SPAWN_EGGS.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptAfter(Items.ALLAY_SPAWN_EGG, JItemRegistry.ANUBIS_USER_SPAWN_EGG.get());
            output.acceptAfter(Items.AXOLOTL_SPAWN_EGG, JItemRegistry.AYA_TSUJI_SPAWN_EGG.get());
            output.acceptAfter(Items.BAT_SPAWN_EGG, JItemRegistry.BRAWLER_SPAWN_EGG.get());
            output.acceptAfter(Items.CREEPER_SPAWN_EGG, JItemRegistry.DARBY_OLDER_SPAWN_EGG.get());
            output.acceptAfter(JItemRegistry.DARBY_OLDER_SPAWN_EGG.get(), JItemRegistry.DARBY_YOUNGER_SPAWN_EGG.get());
            output.acceptAfter(Items.PARROT_SPAWN_EGG, JItemRegistry.PETSHOP_SPAWN_EGG.get());
            output.acceptAfter(Items.TURTLE_SPAWN_EGG, JItemRegistry.VAMPIRE_SPAWN_EGG.get());
        });
    }

    static CreativeModeTab createJcraftItemGroup() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.jcraft.main"))
                .icon(() -> JItemRegistry.STAND_ARROW.get().getDefaultInstance())
                // order of the creative tab
                .displayItems((displayContext, entries) -> {
                    // everything up to arrows
                    entries.accept(JItemRegistry.METEORITE_BLOCK.get());
                    entries.accept(JItemRegistry.POLISHED_METEORITE_BLOCK.get());
                    entries.accept(JItemRegistry.METEORITE_IRON_ORE_BLOCK.get());
                    entries.accept(JItemRegistry.STELLAR_IRON_INGOT.get());
                    entries.accept(JItemRegistry.STELLAR_IRON_BLOCK.get());
                    entries.accept(JItemRegistry.STAND_ARROWHEAD.get());
                    entries.accept(JItemRegistry.STAND_ARROW.get());
                    entries.accept(JItemRegistry.LIVING_ARROW.get());
                    entries.accept(JItemRegistry.REQUIEM_RUBY.get());
                    entries.accept(JItemRegistry.REQUIEM_ARROW.get());
                    // other evolution items
                    entries.accept(JItemRegistry.GREEN_BABY.get());
                    entries.accept(JItemRegistry.DIARY_PAGE.get());
                    entries.accept(JItemRegistry.DIOS_DIARY.get());
                    // stand drops
                    entries.accept(JItemRegistry.FV_REVOLVER.get());
                    entries.accept(JItemRegistry.BULLET.get());
                    entries.accept(JItemRegistry.SCALPEL.get());
                    entries.accept(JItemRegistry.KQ_COIN.get());
                    entries.accept(JItemRegistry.FOOLISH_SAND_BLOCK.get());
                    // misc
                    entries.accept(JItemRegistry.HOT_SAND_BLOCK.get());
                    entries.accept(JItemRegistry.TRAINING_DUMMY.get());
                    entries.accept(JItemRegistry.CINDERELLA_GREEN_BLOCK.get());
                    entries.accept(JItemRegistry.SINNERS_SOUL.get());
                    entries.accept(JItemRegistry.SOUL_BLOCK.get());
                    entries.accept(JItemRegistry.SOUL_WOOD_BLOCK.get());
                    entries.accept(JItemRegistry.KNIFE.get());
                    entries.accept(JItemRegistry.KNIFEBUNDLE.get());
                    entries.accept(JItemRegistry.PRISON_KEY.get());
                    entries.accept(JItemRegistry.PLANKTON_VIAL.get());
                    entries.accept(JItemRegistry.SHIV.get());
                    entries.accept(JItemRegistry.STEEL_BALL.get());
                    // spec items + related except blood bottles
                    entries.accept(JItemRegistry.ANUBIS_SHEATHED.get());
                    entries.accept(JItemRegistry.ANUBIS.get());
                    entries.accept(JItemRegistry.BOXING_GLOVES.get());
                    entries.accept(JItemRegistry.STONE_MASK.get());
                    entries.accept(JItemRegistry.COFFIN_BLOCK.get());
                    entries.accept(JItemRegistry.PEACEMAKER.get());

                    // vehicles
                    entries.accept(JItemRegistry.ROAD_ROLLER.get());
                    // blood bottles
                    for (int i = 16; i >= 0; i--) {
                        final ItemStack stack = new ItemStack(JItemRegistry.BLOOD_BOTTLE.get());
                        stack.getOrCreateTag().putFloat("Blood", i);
                        entries.accept(stack);
                    }
                    // cinderella mask + enchantments
                    entries.accept(JItemRegistry.CINDERELLA_MASK.get());
                    for (int i = 1; i <= 3; i++) {
                        final ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
                        final CompoundTag nbt = stack.getOrCreateTag();
                        final ListTag enchantments = new ListTag();
                        final CompoundTag enchantment = new CompoundTag();
                        enchantment.putString("id", "jcraft:cinderellas_kiss");
                        enchantment.putShort("lvl", (short)i);
                        enchantments.add(enchantment);
                        nbt.put("StoredEnchantments", enchantments);
                        entries.accept(stack);
                    }
                    // spawn eggs part 1
                    entries.accept(JItemRegistry.BRAWLER_SPAWN_EGG.get());
                    entries.accept(JItemRegistry.VAMPIRE_SPAWN_EGG.get());
                    // spawn eggs part 3
                    entries.accept(JItemRegistry.ANUBIS_USER_SPAWN_EGG.get());

                    entries.accept(JItemRegistry.DARBY_OLDER_SPAWN_EGG.get());
                    entries.accept(JItemRegistry.DARBY_YOUNGER_SPAWN_EGG.get());
                    entries.accept(JItemRegistry.PETSHOP_SPAWN_EGG.get());
                    // spawn eggs part 4
                    entries.accept(JItemRegistry.AYA_TSUJI_SPAWN_EGG.get());
                    // weird items
                    if (JItemRegistry.DEBUG_WAND != null) {
                        entries.accept(JItemRegistry.DEBUG_WAND.get());
                    }
                })
                .build();
    }

    static CreativeModeTab createJcraftCosplayGroup() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
                .title(Component.translatable("itemGroup.jcraft.cosplay"))
                .icon(() -> JItemRegistry.DIO_CAPE.get().getDefaultInstance())
                // order of the creative tab
                .displayItems((displayContext, entries) -> {
                    // cosplay
                    entries.accept(JItemRegistry.RED_HAT.get());
                    entries.accept(JItemRegistry.DIO_P1_WIG.get());
                    entries.accept(JItemRegistry.DIO_P1_JACKET.get());
                    entries.accept(JItemRegistry.DIO_P1_PANTS.get());
                    entries.accept(JItemRegistry.DIO_P1_BOOTS.get());
                    entries.accept(JItemRegistry.STRAIZO_PONCHO.get());
                    entries.accept(JItemRegistry.KARS_HEADWRAP.get());
                    entries.accept(JItemRegistry.JOTARO_CAP.get());
                    entries.accept(JItemRegistry.JOTARO_JACKET.get());
                    entries.accept(JItemRegistry.JOTARO_PANTS.get());
                    entries.accept(JItemRegistry.JOTARO_BOOTS.get());
                    entries.accept(JItemRegistry.DIO_HEADBAND.get());
                    entries.accept(JItemRegistry.DIO_JACKET.get());
                    entries.accept(JItemRegistry.DIO_CAPE.get());
                    entries.accept(JItemRegistry.DIO_PANTS.get());
                    entries.accept(JItemRegistry.DIO_BOOTS.get());
                    entries.accept(JItemRegistry.KAKYOIN_WIG.get());
                    entries.accept(JItemRegistry.KAKYOIN_COAT.get());
                    entries.accept(JItemRegistry.KAKYOIN_PANTS.get());
                    entries.accept(JItemRegistry.KAKYOIN_BOOTS.get());
                    entries.accept(JItemRegistry.HEAVEN_ATTAINED_WIG.get());
                    entries.accept(JItemRegistry.HEAVEN_ATTAINED_SHIRT.get());
                    entries.accept(JItemRegistry.HEAVEN_ATTAINED_PANTS.get());
                    entries.accept(JItemRegistry.HEAVEN_ATTAINED_BOOTS.get());
                    entries.accept(JItemRegistry.JOTARO_P4_CAP.get());
                    entries.accept(JItemRegistry.JOTARO_P4_JACKET.get());
                    entries.accept(JItemRegistry.JOTARO_P4_PANTS.get());
                    entries.accept(JItemRegistry.JOTARO_P4_BOOTS.get());
                    entries.accept(JItemRegistry.KIRA_WIG.get());
                    entries.accept(JItemRegistry.KIRA_JACKET.get());
                    entries.accept(JItemRegistry.KIRA_PANTS.get());
                    entries.accept(JItemRegistry.KIRA_BOOTS.get());
                    entries.accept(JItemRegistry.KOSAKU_WIG.get());
                    entries.accept(JItemRegistry.KOSAKU_JACKET.get());
                    entries.accept(JItemRegistry.KOSAKU_PANTS.get());
                    entries.accept(JItemRegistry.KOSAKU_BOOTS.get());
                    entries.accept(JItemRegistry.FINAL_KIRA_WIG.get());
                    entries.accept(JItemRegistry.FINAL_KIRA_JACKET.get());
                    entries.accept(JItemRegistry.FINAL_KIRA_PANTS.get());
                    entries.accept(JItemRegistry.FINAL_KIRA_BOOTS.get());
                    entries.accept(JItemRegistry.GIORNO_WIG.get());
                    entries.accept(JItemRegistry.GIORNO_JACKET.get());
                    entries.accept(JItemRegistry.GIORNO_PANTS.get());
                    entries.accept(JItemRegistry.GIORNO_BOOTS.get());
                    entries.accept(JItemRegistry.RISOTTO_CAP.get());
                    entries.accept(JItemRegistry.RISOTTO_JACKET.get());
                    entries.accept(JItemRegistry.RISOTTO_PANTS.get());
                    entries.accept(JItemRegistry.RISOTTO_BOOTS.get());
                    entries.accept(JItemRegistry.DOPPIO_WIG.get());
                    entries.accept(JItemRegistry.DOPPIO_SHIRT.get());
                    entries.accept(JItemRegistry.DIAVOLO_WIG.get());
                    entries.accept(JItemRegistry.DIAVOLO_SHIRT.get());
                    entries.accept(JItemRegistry.DIAVOLO_PANTS.get());
                    entries.accept(JItemRegistry.DIAVOLO_BOOTS.get());
                    entries.accept(JItemRegistry.PUCCIS_HAT.get());
                    entries.accept(JItemRegistry.PUCCI_ROBE.get());
                    entries.accept(JItemRegistry.PUCCI_PANTS.get());
                    entries.accept(JItemRegistry.PUCCI_BOOTS.get());
                    entries.accept(JItemRegistry.JOHNNY_CAP.get());
                    entries.accept(JItemRegistry.JOHNNY_JACKET.get());
                    entries.accept(JItemRegistry.JOHNNY_PANTS.get());
                    entries.accept(JItemRegistry.JOHNNY_BOOTS.get());
                    entries.accept(JItemRegistry.GYRO_HAT.get());
                    entries.accept(JItemRegistry.GYRO_SHIRT.get());
                    entries.accept(JItemRegistry.GYRO_PANTS.get());
                    entries.accept(JItemRegistry.GYRO_BOOTS.get());
                    entries.accept(JItemRegistry.DIEGO_HAT.get());
                    entries.accept(JItemRegistry.DIEGO_SHIRT.get());
                    entries.accept(JItemRegistry.DIEGO_PANTS.get());
                    entries.accept(JItemRegistry.DIEGO_BOOTS.get());
                    entries.accept(JItemRegistry.RINGO_OUTFIT.get());
                    entries.accept(JItemRegistry.RINGO_BOOTS.get());
                    entries.accept(JItemRegistry.VALENTINE_WIG.get());
                    entries.accept(JItemRegistry.VALENTINE_JACKET.get());
                    entries.accept(JItemRegistry.VALENTINE_PANTS.get());
                    entries.accept(JItemRegistry.VALENTINE_BOOTS.get());
                })
                .build();
    }

    static CreativeModeTab createStandDiscItemGroup() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                .title(Component.translatable("itemGroup.jcraft.discs"))
                .icon(() -> JItemRegistry.DISC.get().getDefaultInstance())
                // order of the creative tab
                .displayItems((displayContext, entries) -> {
                    entries.accept(JItemRegistry.DISC.get());

                    entries.accept(JItemRegistry.SPEC_DISC.get());
                    final List<SpecType> specList = new ArrayList<>(SpecTypeUtil.streamAllRegular().toList());
                    specList.sort(Comparator.comparing(s -> s.getData().getName().getString()));
                    for (final SpecType specType : specList) {
                        entries.accept(SpecDiscItem.createDiscStack(specType));
                    }

                    entries.accept(JItemRegistry.STAND_DISC.get());
                    List<StandType> standList = new ArrayList<>(StandTypeUtil.streamAllRegular().toList());
                    final Comparator<StandType> standComp = Comparator.comparing(s -> s.getData().getInfo().getName().getString());
                    standList.sort(standComp);
                    for (final StandType standType : standList) {
                        for (int skin = 0; skin < standType.getData().getInfo().getSkinCount(); skin++) {
                            entries.accept(StandDiscItem.createDiscStack(standType, skin));
                        }
                    }
                    standList = new ArrayList<>(StandTypeUtil.streamAllEvolutions().toList());
                    standList.sort(standComp);
                    for (final StandType standType : standList) {
                        for (int skin = 0; skin < standType.getData().getInfo().getSkinCount(); skin++) {
                            entries.accept(StandDiscItem.createDiscStack(standType, skin));
                        }
                    }
                })
                .build();
    }

}
