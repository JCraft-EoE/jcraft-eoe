package net.arna.jcraft.client.registry;

import ladysnake.satin.mixin.client.render.RenderLayerAccessor;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.Phases;
import net.arna.jcraft.client.rendering.RenderHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class JRenderLayerRegistry extends RenderPhase {

    public JRenderLayerRegistry(String name, Runnable beginAction, Runnable endAction) {
        super(name, beginAction, endAction);
    }



    public static final RenderLayer TRANSPARENT_BLOCK =
            createGenericRenderLayer(
                    JCraft.MOD_ID,
                    "transparent_block",
                    VertexFormats.POSITION,
                    VertexFormat.DrawMode.QUADS,
                    new Shader(() -> JShaderRegistry.TEST.getInstance().get()),
                    Phases.NORMAL_TRANSPARENCY,
                    SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);


    public static final Function<Identifier, RenderLayer> RRRE = Util.memoize(t -> {
        RenderLayer.MultiPhaseParameters builder = RenderLayer.MultiPhaseParameters.builder()
                .shader(new Shader(() -> JShaderRegistry.RREDE.getInstance().get()))
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .cull(RenderPhase.DISABLE_CULLING)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                .build(true);
        return makeLayer(JCraft.MOD_ID + "", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, builder);

    });



    public static void init() {

    }

    /**
     * Creates a custom render type with a texture.
     */
    public static RenderLayer createGenericRenderLayer(String modId, String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.Shader shader, RenderPhase.Transparency transparency, Identifier texture) {
        return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, new RenderPhase.Texture(texture, false, false));
    }

    /**
     * Creates a custom render type with an empty texture state.
     */
    public static RenderLayer createGenericRenderLayer(String modId, String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.Shader shader, RenderPhase.Transparency transparency, RenderPhase.TextureBase texture) {
        return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, texture);
    }

    /**
     * Creates a custom render type with an empty texture.
     */
    public static RenderLayer createGenericRenderLayer(String modId, String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.Shader shader, RenderPhase.Transparency transparency) {
        return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, NO_TEXTURE);
    }

    private static RenderLayer makeLayer(String name, VertexFormat format, VertexFormat.DrawMode mode, int bufSize, boolean hasCrumbling, boolean sortOnUpload, RenderLayer.MultiPhaseParameters glState) {
        return RenderLayerAccessor.satin$of(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
    }

    /**
     * Creates a custom render type and creates a buffer builder for it.
     */
    public static RenderLayer createGenericRenderLayer(String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.Shader shader, RenderPhase.Transparency transparency, RenderPhase.TextureBase texture) {
        RenderLayer type = RenderLayer.of(
                name, format, mode, FabricLoader.getInstance().isModLoaded("sodium") ? 262144 : 256, false, false, RenderLayer.MultiPhaseParameters.builder()
                        .shader(shader)
                        .transparency(transparency)
                        .texture(texture)
                        .cull(new RenderPhase.Cull(true))
                        .build(true)
        );
        RenderHandler.addRenderLayer(type);
        return type;
    }
}
