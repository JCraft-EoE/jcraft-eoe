package net.arna.jcraft.client.renderer.entity.layer;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.ChariotRequiemEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class SCROutlineLayer extends AbstractRenderLayer<ChariotRequiemEntity> {
    private static final List<ResourceLocation> SKINS = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/stands/chariot_requiem/outline" + i + ".png")).toList();

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, ChariotRequiemEntity> pc) {
        final RenderType cameo = RenderType.eyes(SKINS.get(pc.animatable().getSkin()));
        pc.setRenderType(cameo);
        pc.setVertexConsumer(pc.multiBufferSource().getBuffer(cameo));
        pc.setPackedLight(OverlayTexture.NO_OVERLAY);
        pc.setRed(1f);
        pc.setGreen(1f);
        pc.setBlue(1f);
        pc.setAlpha(1f);
        pc.rendererPipeline().reRender(pc);
    }

}
