package net.arna.jcraft.client.renderer.entity.layer;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.HorusEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class HorusEyesLayer extends AbstractRenderLayer<HorusEntity> {
    private static final ResourceLocation LAYER = new ResourceLocation(JCraft.MOD_ID, "textures/entity/stands/horus/eyes.png");

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, HorusEntity> pc) {
        final RenderType cameo = RenderType.eyes(LAYER);
        pc.setRenderType(cameo);
        pc.setVertexConsumer(pc.multiBufferSource().getBuffer(cameo));
        setDefaultGlow(pc);
        pc.rendererPipeline().reRender(pc);
    }

}
