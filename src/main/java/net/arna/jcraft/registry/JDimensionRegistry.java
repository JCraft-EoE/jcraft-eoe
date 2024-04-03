package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public interface JDimensionRegistry {
    RegistryKey<World> AU_DIMENSION_KEY = RegistryKey.of(RegistryKeys.WORLD, JCraft.id("audim"));
    RegistryKey<DimensionType> AU_TYPE_KEY = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, AU_DIMENSION_KEY.getValue());

    static void registerDimensions() {

    }
}
