package net.arna.jcraft.client.registry;

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
