package net.arna.jcraft.world.dimension;

import net.arna.jcraft.JCraft;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensionRegister {
    public static final RegistryKey<World> AU_DIMENSION_KEY = RegistryKey.of(Registry.WORLD_KEY,
            new Identifier(JCraft.MOD_ID, "audim"));
    public static final RegistryKey<DimensionType> AU_TYPE_KEY = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, AU_DIMENSION_KEY.getValue());

    public static void registerDimensions() {

    }
}
