package net.arna.jcraft.datagen.providers.data;

import net.arna.jcraft.api.registry.JBlockRegistry;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class JTagProviders {
    public static class JBlockTags extends FabricTagProvider.BlockTagProvider {

        public JBlockTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider arg) {
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_PICKAXE).addElement(JBlockRegistry.METEORITE_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_PICKAXE).addElement(JBlockRegistry.POLISHED_METEORITE_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_PICKAXE).addElement(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_PICKAXE).addElement(JBlockRegistry.STELLAR_IRON_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_PICKAXE).addElement(JBlockRegistry.CINDERELLA_GREEN_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.NEEDS_DIAMOND_TOOL).addElement(JBlockRegistry.METEORITE_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.NEEDS_DIAMOND_TOOL).addElement(JBlockRegistry.POLISHED_METEORITE_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.NEEDS_DIAMOND_TOOL).addElement(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.NEEDS_IRON_TOOL).addElement(JBlockRegistry.STELLAR_IRON_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_SHOVEL).addElement(JBlockRegistry.HOT_SAND_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_AXE).addElement(JBlockRegistry.SOUL_WOOD_BLOCK.getId());

            getOrCreateRawBuilder(BlockTags.LOGS_THAT_BURN).addTag(JTagRegistry.SOUL_LOG_BLOCKS.location());
            getOrCreateRawBuilder(BlockTags.SOUL_SPEED_BLOCKS).addElement(JBlockRegistry.SOUL_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.SOUL_SPEED_BLOCKS).addElement(JBlockRegistry.SOUL_WOOD_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.SOUL_FIRE_BASE_BLOCKS).addElement(JBlockRegistry.SOUL_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.SOUL_FIRE_BASE_BLOCKS).addElement(JBlockRegistry.SOUL_WOOD_BLOCK.getId());
            getOrCreateRawBuilder(BlockTags.BEACON_BASE_BLOCKS).addElement(JBlockRegistry.STELLAR_IRON_BLOCK.getId());
            // we do not want bamboo on hot sand, hence we do not add hot sand to the sand tag
            getOrCreateRawBuilder(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON).addElement(JBlockRegistry.HOT_SAND_BLOCK.getId());

            getOrCreateRawBuilder(JTagRegistry.SOUL_LOG_BLOCKS).addElement(JBlockRegistry.SOUL_WOOD_BLOCK.getId());

            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.IRON_BLOCK));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.IRON_BARS));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.IRON_DOOR));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.IRON_TRAPDOOR));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.IRON_ORE));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.DEEPSLATE_IRON_ORE));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.RAW_IRON_BLOCK));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.ANVIL));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.CHIPPED_ANVIL));
            getOrCreateRawBuilder(JTagRegistry.IRON_BLOCKS).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.DAMAGED_ANVIL));

            getOrCreateRawBuilder(JTagRegistry.DUMMY_KNOCKBACK_BLOCKING).addElement(BuiltInRegistries.BLOCK.getKey(Blocks.CUT_RED_SANDSTONE_SLAB));
        }
    }

    public static class JItemTags extends FabricTagProvider.ItemTagProvider {

        public JItemTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider arg) {
            getOrCreateRawBuilder(ItemTags.TRIM_MATERIALS).addElement(JItemRegistry.STELLAR_IRON_INGOT.getId());

            getOrCreateRawBuilder(ItemTags.SAND).addElement(JItemRegistry.HOT_SAND_BLOCK.getId());
            getOrCreateRawBuilder(ItemTags.SMELTS_TO_GLASS).addElement(JItemRegistry.HOT_SAND_BLOCK.getId());

            getOrCreateRawBuilder(ItemTags.BEACON_PAYMENT_ITEMS).addElement(JItemRegistry.STELLAR_IRON_INGOT.getId());
            getOrCreateRawBuilder(ItemTags.BOOKSHELF_BOOKS).addElement(JItemRegistry.DIOS_DIARY.getId());
            getOrCreateRawBuilder(ItemTags.SOUL_FIRE_BASE_BLOCKS).addElement(JItemRegistry.SOUL_BLOCK.getId());
            getOrCreateRawBuilder(ItemTags.ARROWS).addElement(JItemRegistry.STAND_ARROW.getId());

            getOrCreateRawBuilder(JTagRegistry.SOUL_LOG_ITEMS).addElement(JItemRegistry.SOUL_WOOD_BLOCK.getId());

            getOrCreateRawBuilder(JTagRegistry.PROTECTS_FROM_SUN).addElement(JItemRegistry.KARS_HEADWRAP.getId());
            getOrCreateRawBuilder(JTagRegistry.PROTECTS_FROM_SUN).addElement(JItemRegistry.RED_HAT.getId());
            getOrCreateRawBuilder(JTagRegistry.PROTECTS_FROM_SUN).addElement(JItemRegistry.PUCCIS_HAT.getId());
            getOrCreateRawBuilder(JTagRegistry.PROTECTS_FROM_SUN).addElement(JItemRegistry.RISOTTO_CAP.getId());
            getOrCreateRawBuilder(JTagRegistry.PROTECTS_FROM_SUN).addElement(JItemRegistry.DIEGO_HAT.getId());

            final var cosplay = getOrCreateTagBuilder(JTagRegistry.COSPLAY);
            cosplay.add(JItemRegistry.RED_HAT.get());
            cosplay.add(JItemRegistry.DIO_P1_WIG.get());
            cosplay.add(JItemRegistry.DIO_P1_JACKET.get());
            cosplay.add(JItemRegistry.DIO_P1_PANTS.get());
            cosplay.add(JItemRegistry.DIO_P1_BOOTS.get());
            cosplay.add(JItemRegistry.STRAIZO_PONCHO.get());
            cosplay.add(JItemRegistry.KARS_HEADWRAP.get());
            cosplay.add(JItemRegistry.JOTARO_CAP.get());
            cosplay.add(JItemRegistry.JOTARO_JACKET.get());
            cosplay.add(JItemRegistry.JOTARO_PANTS.get());
            cosplay.add(JItemRegistry.JOTARO_BOOTS.get());
            cosplay.add(JItemRegistry.KAKYOIN_WIG.get());
            cosplay.add(JItemRegistry.KAKYOIN_COAT.get());
            cosplay.add(JItemRegistry.KAKYOIN_PANTS.get());
            cosplay.add(JItemRegistry.KAKYOIN_BOOTS.get());
            cosplay.add(JItemRegistry.DIO_HEADBAND.get());
            cosplay.add(JItemRegistry.DIO_CAPE.get());
            cosplay.add(JItemRegistry.DIO_JACKET.get());
            cosplay.add(JItemRegistry.DIO_PANTS.get());
            cosplay.add(JItemRegistry.DIO_BOOTS.get());
            cosplay.add(JItemRegistry.HEAVEN_ATTAINED_WIG.get());
            cosplay.add(JItemRegistry.HEAVEN_ATTAINED_SHIRT.get());
            cosplay.add(JItemRegistry.HEAVEN_ATTAINED_PANTS.get());
            cosplay.add(JItemRegistry.HEAVEN_ATTAINED_BOOTS.get());
            cosplay.add(JItemRegistry.JOTARO_P4_CAP.get());
            cosplay.add(JItemRegistry.JOTARO_P4_JACKET.get());
            cosplay.add(JItemRegistry.JOTARO_P4_PANTS.get());
            cosplay.add(JItemRegistry.JOTARO_P4_BOOTS.get());
            cosplay.add(JItemRegistry.KIRA_WIG.get());
            cosplay.add(JItemRegistry.KIRA_JACKET.get());
            cosplay.add(JItemRegistry.KIRA_PANTS.get());
            cosplay.add(JItemRegistry.KIRA_BOOTS.get());
            cosplay.add(JItemRegistry.KOSAKU_WIG.get());
            cosplay.add(JItemRegistry.KOSAKU_JACKET.get());
            cosplay.add(JItemRegistry.KOSAKU_PANTS.get());
            cosplay.add(JItemRegistry.KOSAKU_BOOTS.get());
            cosplay.add(JItemRegistry.FINAL_KIRA_WIG.get());
            cosplay.add(JItemRegistry.FINAL_KIRA_JACKET.get());
            cosplay.add(JItemRegistry.FINAL_KIRA_PANTS.get());
            cosplay.add(JItemRegistry.FINAL_KIRA_BOOTS.get());
            cosplay.add(JItemRegistry.GIORNO_WIG.get());
            cosplay.add(JItemRegistry.GIORNO_JACKET.get());
            cosplay.add(JItemRegistry.GIORNO_PANTS.get());
            cosplay.add(JItemRegistry.GIORNO_BOOTS.get());
            cosplay.add(JItemRegistry.RISOTTO_CAP.get());
            cosplay.add(JItemRegistry.RISOTTO_JACKET.get());
            cosplay.add(JItemRegistry.RISOTTO_PANTS.get());
            cosplay.add(JItemRegistry.RISOTTO_BOOTS.get());
            cosplay.add(JItemRegistry.DOPPIO_WIG.get());
            cosplay.add(JItemRegistry.DOPPIO_SHIRT.get());
            cosplay.add(JItemRegistry.DIAVOLO_WIG.get());
            cosplay.add(JItemRegistry.DIAVOLO_SHIRT.get());
            cosplay.add(JItemRegistry.DIAVOLO_PANTS.get());
            cosplay.add(JItemRegistry.DIAVOLO_BOOTS.get());
            cosplay.add(JItemRegistry.JOHNNY_CAP.get());
            cosplay.add(JItemRegistry.JOHNNY_JACKET.get());
            cosplay.add(JItemRegistry.JOHNNY_PANTS.get());
            cosplay.add(JItemRegistry.JOHNNY_BOOTS.get());
            cosplay.add(JItemRegistry.GYRO_HAT.get());
            cosplay.add(JItemRegistry.GYRO_SHIRT.get());
            cosplay.add(JItemRegistry.GYRO_PANTS.get());
            cosplay.add(JItemRegistry.GYRO_BOOTS.get());
            cosplay.add(JItemRegistry.DIEGO_HAT.get());
            cosplay.add(JItemRegistry.DIEGO_SHIRT.get());
            cosplay.add(JItemRegistry.DIEGO_PANTS.get());
            cosplay.add(JItemRegistry.DIEGO_BOOTS.get());
            cosplay.add(JItemRegistry.RINGO_OUTFIT.get());
            cosplay.add(JItemRegistry.RINGO_BOOTS.get());
            cosplay.add(JItemRegistry.VALENTINE_WIG.get());
            cosplay.add(JItemRegistry.VALENTINE_JACKET.get());
            cosplay.add(JItemRegistry.VALENTINE_PANTS.get());
            cosplay.add(JItemRegistry.VALENTINE_BOOTS.get());
            cosplay.add(JItemRegistry.PUCCI_ROBE.get());
            cosplay.add(JItemRegistry.PUCCI_PANTS.get());
            cosplay.add(JItemRegistry.PUCCI_BOOTS.get());

            final var equipables = getOrCreateTagBuilder(JTagRegistry.EQUIPABLES);
            equipables.add(Items.LEATHER_HELMET);
            equipables.add(Items.LEATHER_CHESTPLATE);
            equipables.add(Items.LEATHER_LEGGINGS);
            equipables.add(Items.LEATHER_BOOTS);
            equipables.add(Items.CHAINMAIL_HELMET);
            equipables.add(Items.CHAINMAIL_CHESTPLATE);
            equipables.add(Items.CHAINMAIL_LEGGINGS);
            equipables.add(Items.CHAINMAIL_BOOTS);
            equipables.add(Items.IRON_HELMET);
            equipables.add(Items.IRON_CHESTPLATE);
            equipables.add(Items.IRON_LEGGINGS);
            equipables.add(Items.IRON_BOOTS);
            equipables.add(Items.GOLDEN_HELMET);
            equipables.add(Items.GOLDEN_CHESTPLATE);
            equipables.add(Items.GOLDEN_LEGGINGS);
            equipables.add(Items.GOLDEN_BOOTS);
            equipables.add(Items.DIAMOND_HELMET);
            equipables.add(Items.DIAMOND_CHESTPLATE);
            equipables.add(Items.DIAMOND_LEGGINGS);
            equipables.add(Items.DIAMOND_BOOTS);
            equipables.add(Items.NETHERITE_HELMET);
            equipables.add(Items.NETHERITE_CHESTPLATE);
            equipables.add(Items.NETHERITE_LEGGINGS);
            equipables.add(Items.NETHERITE_BOOTS);
            equipables.add(Items.TURTLE_HELMET);
            equipables.add(Items.ELYTRA);
            equipables.add(Items.CARVED_PUMPKIN);
            equipables.add(Items.CREEPER_HEAD);
            equipables.add(Items.DRAGON_HEAD);
            equipables.add(Items.PIGLIN_HEAD);
            equipables.add(Items.PLAYER_HEAD);
            equipables.add(Items.SKELETON_SKULL);
            equipables.add(Items.WITHER_SKELETON_SKULL);
            equipables.add(Items.ZOMBIE_HEAD);
            equipables.add(JItemRegistry.STONE_MASK.get());
            equipables.add(JItemRegistry.KARS_HEADWRAP.get());
            equipables.add(JItemRegistry.RED_HAT.get());
            equipables.addTag(JTagRegistry.COSPLAY);

            final var sandBlocks = getOrCreateTagBuilder(JTagRegistry.SAND_BLOCKS);
            sandBlocks.forceAddTag(ItemTags.SAND);
            sandBlocks.add(Items.SANDSTONE);
            sandBlocks.add(Items.SANDSTONE_STAIRS);
            sandBlocks.add(Items.SANDSTONE_SLAB);
            sandBlocks.add(Items.SANDSTONE_WALL);
            sandBlocks.add(Items.CHISELED_SANDSTONE);
            sandBlocks.add(Items.SMOOTH_SANDSTONE);
            sandBlocks.add(Items.SMOOTH_SANDSTONE_STAIRS);
            sandBlocks.add(Items.SMOOTH_SANDSTONE_SLAB);
            sandBlocks.add(Items.CUT_SANDSTONE);
            sandBlocks.add(Items.CUT_STANDSTONE_SLAB);
            sandBlocks.add(Items.RED_SANDSTONE);
            sandBlocks.add(Items.RED_SANDSTONE_STAIRS);
            sandBlocks.add(Items.RED_SANDSTONE_SLAB);
            sandBlocks.add(Items.RED_SANDSTONE_WALL);
            sandBlocks.add(Items.CHISELED_RED_SANDSTONE);
            sandBlocks.add(Items.SMOOTH_RED_SANDSTONE);
            sandBlocks.add(Items.SMOOTH_RED_SANDSTONE_STAIRS);
            sandBlocks.add(Items.SMOOTH_RED_SANDSTONE_SLAB);
            sandBlocks.add(Items.CUT_RED_SANDSTONE);
            sandBlocks.add(Items.CUT_RED_SANDSTONE_SLAB);

            final var blindsOnImpact = getOrCreateTagBuilder(JTagRegistry.BLINDS_ON_IMPACT);
            blindsOnImpact.add(Items.PACKED_MUD);
            blindsOnImpact.addTag(JTagRegistry.SAND_BLOCKS);
            blindsOnImpact.add(Items.SEA_LANTERN);
            blindsOnImpact.addOptional(ConventionalItemTags.GLASS_BLOCKS.location());
            blindsOnImpact.addOptional(ConventionalItemTags.GLASS_PANES.location());
            blindsOnImpact.add(Items.GRASS_BLOCK);
            blindsOnImpact.add(Items.PODZOL);
            blindsOnImpact.add(Items.DIRT);
            blindsOnImpact.add(Items.COARSE_DIRT);
            // not rooted dirt, it wouldn't fall apart on impact
            blindsOnImpact.add(Items.MUD);
            blindsOnImpact.add(Items.CLAY);
            blindsOnImpact.add(Items.GRAVEL);
            blindsOnImpact.add(Items.SNOW_BLOCK);
            blindsOnImpact.add(Items.CRIMSON_NYLIUM);
            blindsOnImpact.add(Items.WARPED_NYLIUM);
            blindsOnImpact.add(Items.SOUL_SAND);
            blindsOnImpact.add(Items.SOUL_SOIL);
            blindsOnImpact.add(JItemRegistry.SOUL_BLOCK.getId());
            blindsOnImpact.add(Items.NETHERRACK);
            blindsOnImpact.add(Items.GLOWSTONE);
            blindsOnImpact.add(Items.SHROOMLIGHT);
            blindsOnImpact.add(JItemRegistry.DIO_CAPE.getId());
            blindsOnImpact.add(JItemRegistry.KARS_HEADWRAP.getId());

            final var slowsOnImpact = getOrCreateTagBuilder(JTagRegistry.SLOWS_ON_IMPACT);
            slowsOnImpact.add(Items.STICK);
            slowsOnImpact.add(Items.CHAIN);
            slowsOnImpact.add(Items.COBWEB);
            slowsOnImpact.add(Items.BONE);
            slowsOnImpact.add(Items.BLAZE_ROD);

            final var burnsOnImpact = getOrCreateTagBuilder(JTagRegistry.BURNS_ON_IMPACT);
            burnsOnImpact.add(Items.MAGMA_BLOCK);
            burnsOnImpact.add(Items.CAMPFIRE);
            burnsOnImpact.add(Items.SOUL_CAMPFIRE);
            burnsOnImpact.add(Items.LAVA_BUCKET);
            burnsOnImpact.add(Items.FIRE_CHARGE);

            final var poisonsOnImpact = getOrCreateTagBuilder(JTagRegistry.POISONS_ON_IMPACT);
            poisonsOnImpact.add(Items.PUFFERFISH);
            poisonsOnImpact.add(Items.PUFFERFISH_BUCKET);

            final var explodesOnImpact = getOrCreateTagBuilder(JTagRegistry.EXPLODES_ON_IMPACT);
            explodesOnImpact.add(Items.END_CRYSTAL);
            explodesOnImpact.add(Items.TNT);
            explodesOnImpact.add(Items.TNT_MINECART);
            explodesOnImpact.add(Items.FIREWORK_ROCKET);

            final var heavyImpact = getOrCreateTagBuilder(JTagRegistry.HEAVY_IMPACT);
            heavyImpact.add(JItemRegistry.METEORITE_BLOCK.getId());
            heavyImpact.add(JItemRegistry.POLISHED_METEORITE_BLOCK.getId());
            heavyImpact.add(Items.IRON_BLOCK);
            heavyImpact.add(JItemRegistry.STELLAR_IRON_BLOCK.getId());
            heavyImpact.add(Items.GOLD_BLOCK);
            heavyImpact.add(Items.DIAMOND_BLOCK);
            heavyImpact.add(Items.NETHERITE_BLOCK);
            heavyImpact.add(Items.RAW_IRON_BLOCK);
            heavyImpact.add(Items.RAW_GOLD_BLOCK);
            heavyImpact.add(Items.BLAST_FURNACE);
            heavyImpact.add(Items.CAULDRON);
            heavyImpact.forceAddTag(ItemTags.ANVIL);
            heavyImpact.add(Items.CHIPPED_ANVIL);
            heavyImpact.add(Items.DAMAGED_ANVIL);
            heavyImpact.add(Items.OBSIDIAN);
            heavyImpact.add(Items.CRYING_OBSIDIAN);
            heavyImpact.add(Items.RESPAWN_ANCHOR);
            heavyImpact.add(JItemRegistry.ROAD_ROLLER.get());

            final var brittle = getOrCreateTagBuilder(JTagRegistry.BRITTLE);
            brittle.add(Items.ICE);
            brittle.add(Items.PACKED_ICE);
            brittle.add(Items.BLUE_ICE);
            brittle.add(Items.SMALL_AMETHYST_BUD);
            brittle.add(Items.MEDIUM_AMETHYST_BUD);
            brittle.add(Items.LARGE_AMETHYST_BUD);
            brittle.add(Items.AMETHYST_CLUSTER);
            brittle.add(Items.GLOWSTONE);
            brittle.add(Items.GLASS);
            brittle.add(Items.TINTED_GLASS);
            brittle.add(Items.WHITE_STAINED_GLASS);
            brittle.add(Items.LIGHT_GRAY_STAINED_GLASS);
            brittle.add(Items.GRAY_STAINED_GLASS);
            brittle.add(Items.BLACK_STAINED_GLASS);
            brittle.add(Items.BROWN_STAINED_GLASS);
            brittle.add(Items.RED_STAINED_GLASS);
            brittle.add(Items.ORANGE_STAINED_GLASS);
            brittle.add(Items.YELLOW_STAINED_GLASS);
            brittle.add(Items.LIME_STAINED_GLASS);
            brittle.add(Items.GREEN_STAINED_GLASS);
            brittle.add(Items.CYAN_STAINED_GLASS);
            brittle.add(Items.LIGHT_BLUE_STAINED_GLASS);
            brittle.add(Items.BLUE_STAINED_GLASS);
            brittle.add(Items.PURPLE_STAINED_GLASS);
            brittle.add(Items.MAGENTA_STAINED_GLASS);
            brittle.add(Items.PINK_STAINED_GLASS);
            brittle.add(Items.GLASS_PANE);
            brittle.add(Items.WHITE_STAINED_GLASS_PANE);
            brittle.add(Items.LIGHT_GRAY_STAINED_GLASS_PANE);
            brittle.add(Items.GRAY_STAINED_GLASS_PANE);
            brittle.add(Items.BLACK_STAINED_GLASS_PANE);
            brittle.add(Items.BROWN_STAINED_GLASS_PANE);
            brittle.add(Items.RED_STAINED_GLASS_PANE);
            brittle.add(Items.ORANGE_STAINED_GLASS_PANE);
            brittle.add(Items.YELLOW_STAINED_GLASS_PANE);
            brittle.add(Items.LIME_STAINED_GLASS_PANE);
            brittle.add(Items.GREEN_STAINED_GLASS_PANE);
            brittle.add(Items.CYAN_STAINED_GLASS_PANE);
            brittle.add(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
            brittle.add(Items.BLUE_STAINED_GLASS_PANE);
            brittle.add(Items.PURPLE_STAINED_GLASS_PANE);
            brittle.add(Items.MAGENTA_STAINED_GLASS_PANE);
            brittle.add(Items.PINK_STAINED_GLASS_PANE);

            final var superBouncy = getOrCreateTagBuilder(JTagRegistry.SUPER_BOUNCY);
            superBouncy.add(Items.SLIME_BALL);
            superBouncy.add(Items.SLIME_BLOCK);

            final var bouncy = getOrCreateTagBuilder(JTagRegistry.BOUNCY);
            bouncy.addOptionalTag(ItemTags.BUTTONS);
            bouncy.add(Items.BAMBOO);
            bouncy.add(Items.TOTEM_OF_UNDYING);
            bouncy.add(Items.DISC_FRAGMENT_5);
            bouncy.add(JItemRegistry.STAND_ARROWHEAD.get());
            bouncy.add(JItemRegistry.KQ_COIN.get());
            bouncy.add(JItemRegistry.REQUIEM_ARROW.get());

            final var somewhatBouncy = getOrCreateTagBuilder(JTagRegistry.SOMEWHAT_BOUNCY);
            somewhatBouncy.forceAddTag(ItemTags.SWORDS);
            somewhatBouncy.forceAddTag(ItemTags.SHOVELS);
            somewhatBouncy.forceAddTag(ItemTags.PICKAXES);
            somewhatBouncy.forceAddTag(ItemTags.AXES);
            somewhatBouncy.forceAddTag(ItemTags.HOES);
            somewhatBouncy.add(Items.TRIDENT);
            somewhatBouncy.add(Items.COBBLESTONE);
            somewhatBouncy.add(Items.COBBLED_DEEPSLATE);
            somewhatBouncy.add(Items.BONE);
            somewhatBouncy.add(Items.GOAT_HORN);
            somewhatBouncy.add(JItemRegistry.KNIFE.get());
            somewhatBouncy.add(JItemRegistry.SHIV.get());
            somewhatBouncy.add(JItemRegistry.SCALPEL.get());
            somewhatBouncy.add(JItemRegistry.PRISON_KEY.get());

            final var veryHeavy = getOrCreateTagBuilder(JTagRegistry.VERY_HEAVY);
            veryHeavy.forceAddTag(ItemTags.ANVIL);
            veryHeavy.add(Items.NETHERITE_BLOCK);
            veryHeavy.add(JItemRegistry.STELLAR_IRON_BLOCK.get());
            veryHeavy.add(JItemRegistry.ROAD_ROLLER.get());

            final var heavy = getOrCreateTagBuilder(JTagRegistry.HEAVY);
            heavy.add(Items.STONE);
            heavy.add(Items.STONE_STAIRS);
            heavy.add(Items.STONE_SLAB);
            heavy.add(Items.COBBLESTONE);
            heavy.add(Items.COBBLESTONE_STAIRS);
            heavy.add(Items.COBBLESTONE_SLAB);
            heavy.add(Items.COBBLESTONE_WALL);
            heavy.add(Items.MOSSY_COBBLESTONE);
            heavy.add(Items.MOSSY_COBBLESTONE_STAIRS);
            heavy.add(Items.MOSSY_COBBLESTONE_SLAB);
            heavy.add(Items.MOSSY_COBBLESTONE_WALL);
            heavy.add(Items.SMOOTH_STONE);
            heavy.add(Items.SMOOTH_STONE_SLAB);
            heavy.add(Items.STONE_BRICKS);
            heavy.add(Items.CRACKED_STONE_BRICKS);
            heavy.add(Items.STONE_BRICK_STAIRS);
            heavy.add(Items.STONE_BRICK_SLAB);
            heavy.add(Items.STONE_BRICK_WALL);
            heavy.add(Items.CHISELED_STONE_BRICKS);
            heavy.add(Items.MOSSY_STONE_BRICKS);
            heavy.add(Items.MOSSY_STONE_BRICK_STAIRS);
            heavy.add(Items.MOSSY_STONE_BRICK_SLAB);
            heavy.add(Items.MOSSY_STONE_BRICK_WALL);
            heavy.add(Items.GRANITE);
            heavy.add(Items.GRANITE_STAIRS);
            heavy.add(Items.GRANITE_SLAB);
            heavy.add(Items.GRANITE_WALL);
            heavy.add(Items.POLISHED_GRANITE);
            heavy.add(Items.POLISHED_GRANITE_STAIRS);
            heavy.add(Items.POLISHED_GRANITE_SLAB);
            heavy.add(Items.DIORITE);
            heavy.add(Items.DIORITE_STAIRS);
            heavy.add(Items.DIORITE_SLAB);
            heavy.add(Items.DIORITE_WALL);
            heavy.add(Items.POLISHED_DIORITE);
            heavy.add(Items.POLISHED_DIORITE_STAIRS);
            heavy.add(Items.POLISHED_DIORITE_SLAB);
            heavy.add(Items.ANDESITE);
            heavy.add(Items.ANDESITE_STAIRS);
            heavy.add(Items.ANDESITE_SLAB);
            heavy.add(Items.ANDESITE_WALL);
            heavy.add(Items.POLISHED_ANDESITE);
            heavy.add(Items.POLISHED_ANDESITE_STAIRS);
            heavy.add(Items.POLISHED_ANDESITE_SLAB);
            heavy.add(Items.DEEPSLATE);
            heavy.add(Items.COBBLED_DEEPSLATE);
            heavy.add(Items.COBBLED_DEEPSLATE_STAIRS);
            heavy.add(Items.COBBLED_DEEPSLATE_SLAB);
            heavy.add(Items.COBBLED_DEEPSLATE_WALL);
            heavy.add(Items.CHISELED_DEEPSLATE);
            heavy.add(Items.POLISHED_DEEPSLATE);
            heavy.add(Items.POLISHED_DEEPSLATE_STAIRS);
            heavy.add(Items.POLISHED_DEEPSLATE_SLAB);
            heavy.add(Items.POLISHED_DEEPSLATE_WALL);
            heavy.add(Items.DEEPSLATE_BRICKS);
            heavy.add(Items.CRACKED_DEEPSLATE_BRICKS);
            heavy.add(Items.DEEPSLATE_BRICK_STAIRS);
            heavy.add(Items.DEEPSLATE_BRICK_SLAB);
            heavy.add(Items.DEEPSLATE_BRICK_WALL);
            heavy.add(Items.DEEPSLATE_TILES);
            heavy.add(Items.CRACKED_DEEPSLATE_TILES);
            heavy.add(Items.DEEPSLATE_TILE_STAIRS);
            heavy.add(Items.DEEPSLATE_TILE_SLAB);
            heavy.add(Items.DEEPSLATE_TILE_WALL);
            heavy.add(Items.REINFORCED_DEEPSLATE);
            heavy.add(JItemRegistry.METEORITE_BLOCK.get());
            heavy.add(JItemRegistry.POLISHED_METEORITE_BLOCK.get());
            heavy.add(Items.BRICKS);
            heavy.add(Items.BRICK_STAIRS);
            heavy.add(Items.BRICK_SLAB);
            heavy.add(Items.BRICK_WALL);
            heavy.add(Items.BLACKSTONE);
            heavy.add(Items.GILDED_BLACKSTONE);
            heavy.add(Items.BLACKSTONE_STAIRS);
            heavy.add(Items.BLACKSTONE_SLAB);
            heavy.add(Items.BLACKSTONE_WALL);
            heavy.add(Items.CHISELED_POLISHED_BLACKSTONE);
            heavy.add(Items.POLISHED_BLACKSTONE);
            heavy.add(Items.POLISHED_BLACKSTONE_STAIRS);
            heavy.add(Items.POLISHED_BLACKSTONE_SLAB);
            heavy.add(Items.POLISHED_BLACKSTONE_WALL);
            heavy.add(Items.POLISHED_BLACKSTONE_BRICKS);
            heavy.add(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS);
            heavy.add(Items.POLISHED_BLACKSTONE_STAIRS);
            heavy.add(Items.POLISHED_BLACKSTONE_SLAB);
            heavy.add(Items.POLISHED_BLACKSTONE_WALL);
            heavy.add(Items.OBSIDIAN);
            heavy.add(Items.CRYING_OBSIDIAN);
            heavy.add(Items.IRON_BLOCK);
            heavy.add(Items.RAW_IRON_BLOCK);
            heavy.add(Items.GOLD_BLOCK);
            heavy.add(Items.RAW_GOLD_BLOCK);
            heavy.add(Items.EMERALD_BLOCK);
            heavy.add(Items.DIAMOND_BLOCK);
            heavy.add(Items.ANCIENT_DEBRIS);
            heavy.add(Items.NETHERITE_INGOT);
            heavy.forceAddTag(ItemTags.COAL_ORES);
            heavy.forceAddTag(ItemTags.IRON_ORES);
            heavy.forceAddTag(ItemTags.COPPER_ORES);
            heavy.forceAddTag(ItemTags.LAPIS_ORES);
            heavy.forceAddTag(ItemTags.REDSTONE_ORES);
            heavy.forceAddTag(ItemTags.GOLD_ORES);
            heavy.forceAddTag(ItemTags.EMERALD_ORES);
            heavy.forceAddTag(ItemTags.DIAMOND_ORES);
            heavy.add(Items.FURNACE);
            heavy.add(Items.BLAST_FURNACE);
            heavy.add(Items.SMOKER);
            heavy.add(Items.LODESTONE);
            heavy.add(Items.ENCHANTING_TABLE);
            heavy.add(JItemRegistry.STELLAR_IRON_INGOT.get());

            final var light = getOrCreateTagBuilder(JTagRegistry.LIGHT);
            light.forceAddTag(ItemTags.BUTTONS);
            light.forceAddTag(ItemTags.CANDLES);
            light.forceAddTag(ItemTags.WOOL);
            light.forceAddTag(ItemTags.WOODEN_PRESSURE_PLATES);
            light.forceAddTag(ItemTags.LEAVES);
            light.forceAddTag(ItemTags.SAPLINGS);
            light.forceAddTag(ItemTags.FLOWERS);
            light.add(Items.SNOW);
            light.add(Items.SNOWBALL);
            light.add(Items.MOSS_CARPET);
            light.add(Items.SMALL_AMETHYST_BUD);
            light.add(Items.MEDIUM_AMETHYST_BUD);
            light.add(Items.LARGE_AMETHYST_BUD);
            light.add(Items.AMETHYST_CLUSTER);
            light.add(Items.GLOW_LICHEN);
            light.add(Items.HANGING_ROOTS);
            light.add(Items.FROGSPAWN);
            light.add(Items.WHEAT_SEEDS);
            light.add(Items.COCOA_BEANS);
            light.add(Items.PUMPKIN_SEEDS);
            light.add(Items.MELON_SEEDS);
            light.add(Items.BEETROOT_SEEDS);
            light.add(Items.TORCHFLOWER_SEEDS);
            light.add(Items.PITCHER_POD);
            light.add(Items.GLOW_BERRIES);
            light.add(Items.SWEET_BERRIES);
            light.add(Items.NETHER_WART);
            light.add(Items.LILY_PAD);
            light.add(Items.SEAGRASS);
            light.add(Items.SEA_PICKLE);
            light.add(Items.KELP);
            light.add(Items.SCULK_VEIN);
            light.add(Items.COBWEB);
            light.add(Items.BONE_MEAL);
            light.add(Items.NAME_TAG);
            light.add(Items.LEAD);
            light.add(Items.TOTEM_OF_UNDYING);
            light.add(Items.PAPER);
            light.add(Items.MAP);
            light.add(Items.FILLED_MAP);
            light.add(Items.EGG);
            light.add(Items.SPIDER_EYE);
            light.add(Items.COOKIE);
            light.add(Items.AMETHYST_SHARD);
            light.add(Items.IRON_NUGGET);
            light.add(Items.GOLD_NUGGET);
            light.add(Items.FEATHER);
            light.add(Items.STRING);
            light.add(Items.CLAY_BALL);
            light.add(Items.NAUTILUS_SHELL);
            light.add(Items.DISC_FRAGMENT_5);
            light.add(JItemRegistry.PRISON_KEY.get());
            light.add(Items.GHAST_TEAR);
            light.add(Items.SUGAR);
            light.add(Items.GUNPOWDER);
            light.add(Items.FERMENTED_SPIDER_EYE);
            light.add(Items.REDSTONE);
            light.add(Items.GLOWSTONE_DUST);
            light.add(JItemRegistry.DIARY_PAGE.get());

            final var acute = getOrCreateTagBuilder(JTagRegistry.ACUTE);
            acute.forceAddTag(ItemTags.DAMPENS_VIBRATIONS);
            acute.forceAddTag(ItemTags.ANVIL);

            final var obtuse = getOrCreateTagBuilder(JTagRegistry.OBTUSE);
            obtuse.forceAddTag(ItemTags.SWORDS);
            obtuse.forceAddTag(ItemTags.TOOLS);

            final var discs = getOrCreateTagBuilder(JTagRegistry.DISCS);
            discs.add(JItemRegistry.DISC.get());
            discs.add(JItemRegistry.STAND_DISC.get());
            discs.add(JItemRegistry.SPEC_DISC.get());
        }
    }

    public static class JEntityTypeTags extends FabricTagProvider.EntityTypeTagProvider {

        public JEntityTypeTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider arg) {
            getOrCreateRawBuilder(EntityTypeTags.ARROWS).addElement(JEntityTypeRegistry.STAND_ARROW_PROJECTILE.getId());

            // possible mob stand users
            TagBuilder canHaveStandBuilder = getOrCreateRawBuilder(JTagRegistry.CAN_HAVE_STAND);

            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VILLAGER));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WANDERING_TRADER));

            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDERMAN));

            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIGLIN));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIGLIN_BRUTE));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIFIED_PIGLIN));

            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE_VILLAGER));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HUSK));

            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SKELETON));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WITHER_SKELETON));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.STRAY));

            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VINDICATOR));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.EVOKER));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PILLAGER));
            canHaveStandBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WITCH));

            // ferromagnetic entities
            TagBuilder ferrousEntitiesBuilder = getOrCreateRawBuilder(JTagRegistry.FERROUS_ENTITIES);

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.IRON_GOLEM));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.FISHING_BOBBER));

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.MINECART));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CHEST_MINECART));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.FURNACE_MINECART));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HOPPER_MINECART));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.COMMAND_BLOCK_MINECART));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.TNT_MINECART));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SPAWNER_MINECART));

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.TRIDENT));

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.KNIFE.get()));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.SCALPEL.get()));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.BISECT.get()));

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ARROW));
            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SPECTRAL_ARROW));

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.STAND_ARROW_PROJECTILE.get()));

            ferrousEntitiesBuilder.addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.RAZOR.get()));

            // impossible to stun
            getOrCreateRawBuilder(JTagRegistry.CANNOT_BE_STUNNED).addElement(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WARDEN));

            // spec users
            getOrCreateRawBuilder(JTagRegistry.SPEC_USER).addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.BRAWLER_SPEC_USER.get()));
            getOrCreateRawBuilder(JTagRegistry.SPEC_USER).addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.VAMPIRE_SPEC_USER.get()));
            getOrCreateRawBuilder(JTagRegistry.SPEC_USER).addElement(BuiltInRegistries.ENTITY_TYPE.getKey(JEntityTypeRegistry.ANUBIS_SPEC_USER.get()));

            // stands
            final var stands = getOrCreateTagBuilder(JTagRegistry.STANDS);
            for (final StandType stand : StandTypeUtil.streamAll().toList()) {
                if (stand == JStandTypeRegistry.NONE.get()) {
                    continue;
                }
                stands.add(BuiltInRegistries.ENTITY_TYPE.getKey(stand.getEntityType()));
            }

            final var neverStands = getOrCreateTagBuilder(JTagRegistry.CAN_NEVER_HAVE_STAND);
            neverStands.addTag(JTagRegistry.STANDS);
            neverStands.add(JEntityTypeRegistry.SHEER_HEART_ATTACK.getId());
            neverStands.add(JEntityTypeRegistry.LIFE_DETECTOR.getId());
            neverStands.add(JEntityTypeRegistry.GE_FROG.getId());
            neverStands.add(JEntityTypeRegistry.GE_BUTTERFLY.getId());
            neverStands.add(JEntityTypeRegistry.GE_SNAKE.getId());
            neverStands.add(JEntityTypeRegistry.GER_SCORPION.getId());
            neverStands.add(JEntityTypeRegistry.PLAYER_CLONE.getId());
            neverStands.add(JEntityTypeRegistry.HG_NET.getId());
            neverStands.add(JEntityTypeRegistry.RED_BIND.getId());
            neverStands.add(JEntityTypeRegistry.SAND_TORNADO.getId());
            neverStands.add(JEntityTypeRegistry.STAND_METEOR.getId());

            final var noAIStandUsers = getOrCreateTagBuilder(JTagRegistry.NO_STAND_USER_AI);
            noAIStandUsers.add(JEntityTypeRegistry.TRAINING_DUMMY.getId());

            addTagsForCompatibilities(arg);
        }

        private void addTagsForCompatibilities(HolderLookup.Provider arg) {
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("ad_astra", "can_survive_extreme_cold")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("ad_astra", "can_survive_extreme_heat")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("ad_astra", "can_survive_in_acid_rain")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("ad_astra", "can_survive_in_space")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("ad_astra", "ignores_air_vortex")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("ad_astra", "lives_without_oxygen")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("carryon", "entity_blacklist")))
                    .addTag(JTagRegistry.STANDS);
            getOrCreateTagBuilder(TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("irons_spellbooks", "cant_root")))
                    .addTag(JTagRegistry.STANDS);
        }
    }

}
