package net.arna.jcraft.fabric.datagen;

import net.arna.jcraft.api.registry.JBiomeRegistry;
import net.arna.jcraft.api.registry.JPlacedFeatureRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

// see https://minecraft.fandom.com/wiki/Custom_biome
public class JBiomeProvider {

    public static void bootstrap(final BootstapContext<Biome> context) {
        context.register(JBiomeRegistry.DEVILS_PALM, devilsPalm(context));
        context.register(JBiomeRegistry.TUTORIAL, tutorial(context));
    }

    private static Biome devilsPalm(final BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomeGenerationSettings.Builder generationBuilder =
                new BiomeGenerationSettings.Builder(context.lookup(Registries.PLACED_FEATURE),
                        context.lookup(Registries.CONFIGURED_CARVER));
        generationBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, JPlacedFeatureRegistry.SAND_DISK);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .downfall(0f)
                .temperature(3f)
                .generationSettings(generationBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(10203353)
                        .waterFogColor(6645121)
                        .skyColor(14987035)
                        .fogColor(16314057)
                        .build())
                .build();
    }

    private static Biome tutorial(final BootstapContext<Biome> context) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .downfall(0f)
                .temperature(0f)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        // plains properties
                        .waterColor(0x3f76e4)
                        .waterFogColor(0x050533)
                        .skyColor(0x78a7ff)
                        .fogColor(0xc0d8ff)
                        .build())
                .build();
    }

}
