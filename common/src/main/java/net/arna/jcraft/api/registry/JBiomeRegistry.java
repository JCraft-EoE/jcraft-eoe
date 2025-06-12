package net.arna.jcraft.api.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public interface JBiomeRegistry {
    ResourceKey<Biome> DEVILS_PALM = ResourceKey.create(Registries.BIOME, JCraft.id("devils_palm"));
    ResourceKey<Biome> TUTORIAL = ResourceKey.create(Registries.BIOME, JCraft.id("tutorial"));
}
