package net.arna.jcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class JDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();

        pack.addProvider(JModelProvider::new);
        //generator.addProvider(JLanguageProvider::new);
        pack.addProvider(JLootTableProviders.BlockLoot::new);
        pack.addProvider(JLootTableProviders.EntityLoot::new);
        //pack.addProvider(JTagProviders.JBlockTags::new);
        //pack.addProvider(JTagProviders.JItemTags::new);
        pack.addProvider(JAdvancementProvider::new);
        pack.addProvider(JRecipeProvider::new);
    }
}
