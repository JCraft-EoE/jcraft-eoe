package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public interface JDimensionRegister {
    RegistryKey<World> AU_DIMENSION_KEY = RegistryKey.of(Registry.WORLD_KEY, JCraft.id("audim"));
    RegistryKey<DimensionType> AU_TYPE_KEY = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, AU_DIMENSION_KEY.getValue());

    static void registerDimensions() {

    }
}
