package net.arna.jcraft.mixin.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

/**
 * Exposes the (protected) texture of a {@link RenderStateShard.TextureStateShard}, used by the afterimage fade to
 * recreate a translucent version of each render type.
 */
@Mixin(RenderStateShard.TextureStateShard.class)
public interface TextureStateShardAccessor {
    @Invoker("cutoutTexture")
    Optional<ResourceLocation> jcraft$cutoutTexture();
}
