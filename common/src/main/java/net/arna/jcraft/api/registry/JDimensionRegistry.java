package net.arna.jcraft.api.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public interface JDimensionRegistry {
    ResourceKey<Level> AU_DIMENSION_KEY = ResourceKey.create(Registries.DIMENSION, JCraft.id("audim"));
    ResourceKey<DimensionType> AU_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, AU_DIMENSION_KEY.location());
    ResourceKey<Level> TUTORIAL_DIMENSION_KEY = ResourceKey.create(Registries.DIMENSION, JCraft.id("tutorial"));
    ResourceKey<DimensionType> TUTORIAL_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, TUTORIAL_DIMENSION_KEY.location());

    static void init() {
        // intentionally left empty
    }
}
