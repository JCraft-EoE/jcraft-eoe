package net.arna.jcraft.registry;

import net.arna.jcraft.client.rendering.RenderHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;

public class JRenderLayerRegistry extends RenderPhase {

    public JRenderLayerRegistry(String name, Runnable beginAction, Runnable endAction) {
        super(name, beginAction, endAction);
    }

    public static void init() {

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
