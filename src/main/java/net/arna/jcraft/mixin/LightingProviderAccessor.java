package net.arna.jcraft.mixin;

import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightingProvider.class)
public interface LightingProviderAccessor {
    @Accessor
    ChunkLightProvider<?, ?> getBlockLightProvider();

    @Accessor
    ChunkLightProvider<?, ?> getSkyLightProvider();
}
