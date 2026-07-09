package net.arna.jcraft.client.renderer.entity.layer;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class MRGlowLayer extends AbstractRenderLayer<MagiciansRedEntity> {
    private static final ResourceLocation LAYER = new ResourceLocation(JCraft.MOD_ID, "textures/entity/stands/magicians_red/glow.png");

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, MagiciansRedEntity> pc) {
        final RenderType cameo = RenderType.eyes(LAYER);
        pc.setRenderType(cameo);
        pc.setVertexConsumer(pc.multiBufferSource().getBuffer(cameo));
        setDefaultGlow(pc);
        pc.rendererPipeline().reRender(pc);
    }

}
