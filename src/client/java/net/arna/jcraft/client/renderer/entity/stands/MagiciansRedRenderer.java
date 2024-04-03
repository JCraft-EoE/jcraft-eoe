package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.MagiciansRedModel;
import net.arna.jcraft.client.renderer.entity.layer.MRGlowLayer;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;

public class MagiciansRedRenderer extends StandEntityRenderer<MagiciansRedEntity> {
    public MagiciansRedRenderer(EntityRendererFactory.Context context) {
        super(context, new MagiciansRedModel());
        addLayer(new MRGlowLayer(this));
    }

    @Override
    public void render(GeoModel model, MagiciansRedEntity stand, float tickDelta, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexConsumer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.render(model, stand, tickDelta, type, matrixStackIn, vertexConsumerProvider, vertexConsumer, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (stand.getState() == MagiciansRedEntity.State.RED_BIND) {
            if (MinecraftClient.getInstance().isPaused()) return;
            model.getBone("rope3").ifPresent(bone -> {
                Vector3d worldPos = bone.getWorldPosition();

                stand.getEntityWorld().addParticle(ParticleTypes.FLAME,
                        worldPos.x, worldPos.y, worldPos.z,
                        0.0, 0.0, 0.0
                );
            });
        }
    }
}
