package net.arna.jcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class JDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        generator.addProvider(JModelProvider::new);
        //generator.addProvider(JLanguageProvider::new);
        generator.addProvider(JLootTableProviders.BlockLoot::new);
        generator.addProvider(JLootTableProviders.EntityLoot::new);
        generator.addProvider(JTagProviders.JBlockTags::new);
        generator.addProvider(JTagProviders.JItemTags::new);
    }
}
