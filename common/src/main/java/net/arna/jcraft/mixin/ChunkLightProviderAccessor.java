package net.arna.jcraft.mixin;

import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightEngine.class)
public interface ChunkLightProviderAccessor {

    @Accessor
    LayerLightSectionStorage<?> getStorage();

}
