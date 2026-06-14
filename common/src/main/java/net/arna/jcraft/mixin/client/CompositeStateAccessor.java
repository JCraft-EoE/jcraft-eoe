package net.arna.jcraft.mixin.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the {@code textureState} of a render type's composite state. Narrowed by callers via
 * {@link TextureStateShardAccessor}.
 */
@Mixin(RenderType.CompositeState.class)
public interface CompositeStateAccessor {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard jcraft$getTextureState();
}
