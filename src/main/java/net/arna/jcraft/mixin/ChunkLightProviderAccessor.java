package net.arna.jcraft.mixin;

import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkLightProvider.class)
public interface ChunkLightProviderAccessor {
    @Accessor
    LightStorage<?> getLightStorage();
}
