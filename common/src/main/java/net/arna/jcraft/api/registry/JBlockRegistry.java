package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.common.block.FoolishSandBlock;
import net.arna.jcraft.common.block.HotSandBlock;
import net.arna.jcraft.common.block.SoulBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;


public interface JBlockRegistry {

    DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, Registries.BLOCK);

    //Block
    RegistrySupplier<Block> FOOLISH_SAND_BLOCK = BLOCK_REGISTRY.register("foolish_sand_block", () -> new FoolishSandBlock(BlockBehaviour.Properties.of()
            .strength(0.5f)
            .sound(SoundType.SAND)
    ));
    RegistrySupplier<Block> SOUL_BLOCK = BLOCK_REGISTRY.register("soul_block", () -> new SoulBlock(BlockBehaviour.Properties.of()
            .strength(4.0f)
            .sound(SoundType.SOUL_SOIL)
    ));
    RegistrySupplier<Block> METEORITE_BLOCK = BLOCK_REGISTRY.register("meteorite_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .requiresCorrectToolForDrops()
            .strength(6.0f, 1200f)
            .sound(SoundType.ANCIENT_DEBRIS)
            .mapColor(MapColor.COLOR_BLACK)
    ));
    RegistrySupplier<Block> POLISHED_METEORITE_BLOCK = BLOCK_REGISTRY.register("polished_meteorite_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .requiresCorrectToolForDrops()
            .strength(6.0f, 1200f)
            .sound(SoundType.ANCIENT_DEBRIS)
            .mapColor(MapColor.COLOR_BLACK)
    ));
    RegistrySupplier<Block> METEORITE_IRON_ORE_BLOCK = BLOCK_REGISTRY.register("meteorite_iron_ore_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)
            .requiresCorrectToolForDrops()
            .strength(9.0f, 1200f)
            .sound(SoundType.ANCIENT_DEBRIS)
            .mapColor(MapColor.COLOR_BLACK)
    ));
    RegistrySupplier<Block> STELLAR_IRON_BLOCK = BLOCK_REGISTRY.register("stellar_iron_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
            .requiresCorrectToolForDrops()
            .strength(7.5f, 1000f)
            .sound(SoundType.NETHERITE_BLOCK)
            .mapColor(MapColor.COLOR_YELLOW)
    ));
    RegistrySupplier<Block> HOT_SAND_BLOCK = BLOCK_REGISTRY.register("hot_sand_block", () -> new HotSandBlock(BlockBehaviour.Properties.of()
            .strength(0.5f)
            .sound(SoundType.SAND)
            .mapColor(MapColor.COLOR_ORANGE)
    ));
    RegistrySupplier<Block> CINDERELLA_GREEN_BLOCK = BLOCK_REGISTRY.register("cinderella_green_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GREEN_TERRACOTTA)
    ));
    RegistrySupplier<Block> SOUL_WOOD_BLOCK = BLOCK_REGISTRY.register("soul_wood_block", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD)
    ));
    RegistrySupplier<Block> COFFIN_BLOCK = BLOCK_REGISTRY.register("coffin", () -> new CoffinBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).noOcclusion()));


    static void init() {

    }
}
