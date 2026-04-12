package net.arna.jcraft.client.renderer.entity.layer;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class HGNetGlowLayer extends AbstractRenderLayer<HGNetEntity> {
    private static final List<ResourceLocation> SKINS = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/hg_nets/glow_" + i + ".png")).toList();

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, HGNetEntity> pc) {
        final HGNetEntity net = pc.animatable();
        if (net.isCharged()) {
            final RenderType cameo = RenderType.eyes(SKINS.get(net.getSkin()));
            pc.setRenderType(cameo);
            pc.setVertexConsumer(pc.multiBufferSource().getBuffer(cameo));
            setDefaultGlow(pc);
            pc.rendererPipeline().reRender(pc);
        }
    }

}
