package net.arna.jcraft.datagen;

import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.datagen.providers.data.*;
import net.arna.jcraft.datagen.providers.assets.JLangProvider;
import net.arna.jcraft.datagen.providers.assets.JModelProvider;
import net.arna.jcraft.datagen.providers.assets.JPoseProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public final class JDataGen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        Util.init();

        final FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(JModelProvider::new);
        //pack.addProvider(JLanguageProvider::new);
        pack.addProvider(JLootTableProviders.BlockLoot::new);
        pack.addProvider(JLootTableProviders.EntityLoot::new);
        pack.addProvider(JTagProviders.JBlockTags::new);
        pack.addProvider(JTagProviders.JItemTags::new);
        pack.addProvider(JTagProviders.JEntityTypeTags::new);
        pack.addProvider(JTagProviders.JTemplatePoolTags::new);
        pack.addProvider(JAdvancementProvider::new);
        pack.addProvider(JRecipeProvider::new);
        pack.addProvider(JWorldProvider::new);
        pack.addProvider(JEvolutionProvider::new);
        pack.addProvider(JStandDataProvider::new);
        pack.addProvider(JSpecDataProvider::new);
        pack.addProvider(JPoseProvider::new);
        pack.addProvider(JLangProvider::new);

        // Each type needs its own MoveSetProvider as they have different state classes
        // and thus different codecs.
        Set<ResourceLocation> ids = new HashSet<>(JRegistries.STAND_TYPE_REGISTRY.getIds());
        ids.addAll(JRegistries.SPEC_TYPE_REGISTRY.getIds());
        for (final ResourceLocation type : ids) {
            if ("none".equals(type.getPath())) continue;
            pack.addProvider((FabricDataOutput output) -> new JMoveSetProvider<>(output, type));
        }
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.CONFIGURED_FEATURE, JConfiguredFeatureProvider::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, JPlacedFeatureProvider::bootstrap);
        registryBuilder.add(Registries.BIOME, JBiomeProvider::bootstrap);
    }
}
