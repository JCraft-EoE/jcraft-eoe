package net.arna.jcraft.fabric.common.terrablender;

import com.mojang.datafixers.util.Pair;
import net.arna.jcraft.api.registry.JBiomeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;
import terrablender.api.ParameterUtils.*;

import java.util.function.Consumer;

public class OverworldRegionFabric extends Region {
    public OverworldRegionFabric(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    // see https://minecraft.fandom.com/wiki/Biome#Overworld_3
    @Override
    public void addBiomes(
            Registry<Biome> registry,
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper
    ) {
/*    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();
        // Devil's Palm: The parameters for this biome are chosen to resemble the ones for the desert biome.
        new ParameterPointListBuilder()
                .temperature(Temperature.HOT)
                .humidity(Humidity.FULL_RANGE)
                .continentalness(Continentalness.MID_INLAND)
                .erosion(Erosion.EROSION_4, Erosion.EROSION_6)
                .depth(Depth.SURFACE)
                .weirdness(Weirdness.FULL_RANGE)
                .build().forEach(point -> builder.add(point, JBiomeRegistry.DEVILS_PALM));
        // add our points to the mapper
        builder.build().forEach(mapper);*/
    }
}
