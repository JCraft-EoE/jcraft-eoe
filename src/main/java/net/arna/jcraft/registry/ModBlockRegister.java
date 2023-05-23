package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.block.SoulBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockRegister {
    public static final Block SOUL_BLOCK = new SoulBlock(FabricBlockSettings.of(Material.DENSE_ICE).strength(4.0f));

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(JCraft.MOD_ID, "soul_block"), SOUL_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(JCraft.MOD_ID, "soul_block"), new BlockItem(SOUL_BLOCK, new FabricItemSettings()));
    }
}
