package net.arna.jcraft.registry;

import dev.architectury.registry.CreativeTabRegistry;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.BrainType;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.item.BrainDiscItem;
import net.arna.jcraft.common.item.StandDiscItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public interface JCreativeMenuTabRegistry {
    @SuppressWarnings("UnstableApiUsage") // we do not care :)
    static void init() {
        JCraft.CREATIVE_TAB_REGISTRY.register("general", JCreativeMenuTabRegistry::createJcraftItemGroup);
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
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.NATURAL_BLOCKS.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.CANDLE, JItemRegistry.COFFIN_BLOCK.get());
        });
        // tools
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.COMPASS, JItemRegistry.STAND_ARROW.get());
            output.acceptBefore(Items.COMPASS, JItemRegistry.STAND_DISC.get());
        });
        // combat
        CreativeTabRegistry.modifyBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.COMBAT.location()), (flags, output, canUseGameMasterBlocks) -> {
            output.acceptBefore(Items.WOODEN_SWORD, JItemRegistry.SHIV.get());
            output.acceptBefore(Items.WOODEN_AXE, JItemRegistry.ANUBIS_SHEATHED.get());
            output.acceptBefore(Items.BOW, JItemRegistry.KNIFE.get());
            output.acceptBefore(Items.BOW, JItemRegistry.KNIFEBUNDLE.get());
            output.acceptBefore(Items.BOW, JItemRegistry.FV_REVOLVER.get());
            output.acceptBefore(Items.BOW, JItemRegistry.BULLET.get());
            output.acceptAfter(Items.NETHERITE_BOOTS, JItemRegistry.JOTARO_CAP.get());
            output.acceptAfter(JItemRegistry.JOTARO_CAP.get(), JItemRegistry.JOTARO_JACKET.get());
            output.acceptAfter(JItemRegistry.JOTARO_JACKET.get(), JItemRegistry.JOTARO_PANTS.get());
            output.acceptAfter(JItemRegistry.JOTARO_PANTS.get(), JItemRegistry.JOTARO_BOOTS.get());
            output.acceptAfter(JItemRegistry.JOTARO_BOOTS.get(), JItemRegistry.DIO_HEADBAND.get());
            output.acceptAfter(JItemRegistry.DIO_HEADBAND.get(), JItemRegistry.DIO_JACKET.get());
            output.acceptAfter(JItemRegistry.DIO_JACKET.get(), JItemRegistry.DIO_CAPE.get());
            output.acceptAfter(JItemRegistry.DIO_CAPE.get(), JItemRegistry.DIO_PANTS.get());
            output.acceptAfter(JItemRegistry.DIO_PANTS.get(), JItemRegistry.DIO_BOOTS.get());
            output.acceptBefore(Items.SHIELD, JItemRegistry.BOXING_GLOVES.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.STONE_MASK.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.RED_HAT.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.KARS_HEADWRAP.get());
            output.acceptBefore(Items.LEATHER_HORSE_ARMOR, JItemRegistry.PUCCIS_HAT.get());
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
            output.acceptAfter(Items.AXOLOTL_SPAWN_EGG, JItemRegistry.AYA_TSUJI_SPAWN_EGG.get());
            output.acceptAfter(Items.CREEPER_SPAWN_EGG, JItemRegistry.DARBY_OLDER_SPAWN_EGG.get());
            output.acceptAfter(JItemRegistry.DARBY_OLDER_SPAWN_EGG.get(), JItemRegistry.DARBY_YOUNGER_SPAWN_EGG.get());
            output.acceptAfter(Items.PARROT_SPAWN_EGG, JItemRegistry.PETSHOP_SPAWN_EGG.get());
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
                    entries.accept(JItemRegistry.KQ_COIN.get());
                    entries.accept(JItemRegistry.FOOLISH_SAND_BLOCK.get());
                    // misc
                    entries.accept(JItemRegistry.HOT_SAND_BLOCK.get());
                    entries.accept(JItemRegistry.CINDERELLA_GREEN_BLOCK.get());
                    entries.accept(JItemRegistry.SINNERS_SOUL.get());
                    entries.accept(JItemRegistry.SOUL_BLOCK.get());
                    entries.accept(JItemRegistry.SOUL_WOOD_BLOCK.get());
                    entries.accept(JItemRegistry.KNIFE.get());
                    entries.accept(JItemRegistry.KNIFEBUNDLE.get());
                    entries.accept(JItemRegistry.PRISON_KEY.get());
                    entries.accept(JItemRegistry.PLANKTON_VIAL.get());
                    entries.accept(JItemRegistry.SHIV.get());
                    // spec items + related except blood bottles
                    entries.accept(JItemRegistry.ANUBIS_SHEATHED.get());
                    entries.accept(JItemRegistry.ANUBIS.get());
                    entries.accept(JItemRegistry.BOXING_GLOVES.get());
                    entries.accept(JItemRegistry.STONE_MASK.get());
                    entries.accept(JItemRegistry.RED_HAT.get());
                    entries.accept(JItemRegistry.KARS_HEADWRAP.get());
                    entries.accept(JItemRegistry.PUCCIS_HAT.get());
                    entries.accept(JItemRegistry.COFFIN_BLOCK.get());
                    // cosplay
                    entries.accept(JItemRegistry.JOTARO_CAP.get());
                    entries.accept(JItemRegistry.JOTARO_JACKET.get());
                    entries.accept(JItemRegistry.JOTARO_PANTS.get());
                    entries.accept(JItemRegistry.JOTARO_BOOTS.get());
                    entries.accept(JItemRegistry.DIO_HEADBAND.get());
                    entries.accept(JItemRegistry.DIO_JACKET.get());
                    entries.accept(JItemRegistry.DIO_CAPE.get());
                    entries.accept(JItemRegistry.DIO_PANTS.get());
                    entries.accept(JItemRegistry.DIO_BOOTS.get());
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
                    // stand discs
                    for (final StandType standType : StandType.getRegularStandTypes()) {
                        for (int skin = 0; skin < standType.getSkinCount(); skin++) {
                            entries.accept(StandDiscItem.createDiscStack(standType, skin));
                        }
                    }
                    for (final StandType standType : StandType.getEvoStandTypes()) {
                        for (int skin = 0; skin < standType.getSkinCount(); skin++) {
                            entries.accept(StandDiscItem.createDiscStack(standType, skin));
                        }
                    }
                    entries.accept(BrainDiscItem.createDiscStack(null));
                    for (final BrainType brainType : BrainType.values()) {
                        entries.accept(BrainDiscItem.createDiscStack(brainType));
                    }
                    // spawn eggs season 3
                    entries.accept(JItemRegistry.DARBY_OLDER_SPAWN_EGG.get());
                    entries.accept(JItemRegistry.DARBY_YOUNGER_SPAWN_EGG.get());
                    entries.accept(JItemRegistry.PETSHOP_SPAWN_EGG.get());
                    // spawn eggs season 4
                    entries.accept(JItemRegistry.AYA_TSUJI_SPAWN_EGG.get());
                    // weird items
                    if (JItemRegistry.DEBUG_WAND != null) {
                        entries.accept(JItemRegistry.DEBUG_WAND.get());
                    }
                })
                .build();
    }

}
