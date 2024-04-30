package net.arna.jcraft.datagen;

import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;

public class JModelProvider extends FabricModelProvider {
    public JModelProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.registerSimpleCubeAll(JObjectRegistry.METEORITE_BLOCK);
        generator.registerSimpleCubeAll(JObjectRegistry.METEORITE_IRON_ORE_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
    }
}
