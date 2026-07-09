package net.arna.jcraft.client.renderer.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.ShadowTheWorldEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class STWGlowLayer extends AbstractRenderLayer<ShadowTheWorldEntity> {

    private static final List<ResourceLocation> SKINS = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/stands/shadow_the_world/"+ "shade_" + i + ".png")).toList();

    @Override
    public void render(final @NonNull AzRendererPipelineContext<UUID, ShadowTheWorldEntity> pc) {
        final ShadowTheWorldEntity stw = pc.animatable();
        final RenderType cameo = RenderType.entityTranslucentEmissive(SKINS.get(stw.getSkin()));
        final PoseStack poseStack = pc.poseStack();
        poseStack.pushPose();
        final float tick = (stw.tickCount + pc.partialTick()) * 3.1415f / 10.0f;
        final float mod = 0.015f;
        final float x = Mth.sin(tick) * mod,
                y = Mth.cos(tick) * mod,
                z = Mth.cos(tick) * mod;
        pc.setRenderType(cameo);
        pc.setVertexConsumer(pc.multiBufferSource().getBuffer(cameo));
        setDefaultGlow(pc);
        poseStack.translate(x, y, z);
        pc.rendererPipeline().reRender(pc);
        poseStack.translate(-x * 2, -y * 2, -z * 2);
        pc.rendererPipeline().reRender(pc);
        poseStack.popPose();
    }

}
