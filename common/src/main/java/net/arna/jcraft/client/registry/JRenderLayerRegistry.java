package net.arna.jcraft.client.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.architectury.platform.Platform;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.Phases;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.arna.jcraft.platform.JPlatformUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class JRenderLayerRegistry extends RenderStateShard {

    public JRenderLayerRegistry(final String name, final Runnable beginAction, final Runnable endAction) {
        super(name, beginAction, endAction);
    }

    public static final RenderType TRANSPARENT_BLOCK =
            createGenericRenderLayer(
                    JCraft.MOD_ID,
                    "transparent_block",
                    DefaultVertexFormat.POSITION,
                    VertexFormat.Mode.QUADS,
                    new ShaderStateShard(JPlatformUtils::getTest),
                    Phases.NORMAL_TRANSPARENCY,
                    TextureAtlas.LOCATION_PARTICLES);

    public static final RenderType RRRE =
            createGenericRenderLayer(
                    JCraft.MOD_ID,
                    "rrre",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    new ShaderStateShard(JPlatformUtils::getRred),
                    RenderStateShard.TRANSLUCENT_TRANSPARENCY
            );

    public static void init() {

    }

    /**
     * Creates a custom render layer with a texture.
     */
    public static RenderType createGenericRenderLayer(final String modId, final String name, final VertexFormat format, final VertexFormat.Mode mode, final ShaderStateShard shader, final TransparencyStateShard transparency, final ResourceLocation texture) {
        return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, new TextureStateShard(texture, false, false));
    }

    /**
     * Creates a custom render layer with an empty texture state.
     */
    public static RenderType createGenericRenderLayer(final String modId, final String name, final VertexFormat format, final VertexFormat.Mode mode, final ShaderStateShard shader, final TransparencyStateShard transparency, final EmptyTextureStateShard texture) {
        return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, texture);
    }

    /**
     * Creates a custom render layer with an empty texture.
     */
    public static RenderType createGenericRenderLayer(final String modId, final String name, final VertexFormat format, final VertexFormat.Mode mode, final ShaderStateShard shader, final TransparencyStateShard transparency) {
        return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, NO_TEXTURE);
    }

    /**
     * Creates a custom render layer and creates a buffer builder for it.
     */
    public static RenderType createGenericRenderLayer(final String name, final VertexFormat format, final VertexFormat.Mode mode, final ShaderStateShard shader, final TransparencyStateShard transparency, final EmptyTextureStateShard texture) {
        final RenderType type = RenderType.create(//TODO add rubidium etc?
                name, format, mode, Platform.isModLoaded("sodium") ? 262144 : 256, false, false, RenderType.CompositeState.builder()
                        .setShaderState(shader)
                        .setTransparencyState(transparency)
                        .setTextureState(texture)
                        .setCullState(new CullStateShard(true))
                        .createCompositeState(true)
        );
        RenderHandler.addRenderLayer(type);
        return type;
    }
}
