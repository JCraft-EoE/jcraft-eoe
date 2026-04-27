package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.MatchProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MatchRenderer extends EntityRenderer<MatchProjectile> {
    private static final ResourceLocation texture = JCraft.id("textures/entity/projectiles/match.png");

    public MatchRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(@NotNull MatchProjectile entity, @NotNull BlockPos pos) {
        // They're on fire, so fullbright.
        return 15;
    }

    @Override
    public void render(MatchProjectile entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (entity.tickCount < 2 && this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)
            return;

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(texture));
        PoseStack.Pose pose = poseStack.last();

        int frame = entity.tickCount % 4;
        float v0 = frame * 0.25f;
        float v1 = v0 + 0.25f;

        float s = 0.25f;
        vc.vertex(pose.pose(), -s, -s, 0).color(255, 255, 255, 255).uv(0, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0, 0, 1).endVertex();
        vc.vertex(pose.pose(),  s, -s, 0).color(255, 255, 255, 255).uv(1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0, 0, 1).endVertex();
        vc.vertex(pose.pose(),  s,  s, 0).color(255, 255, 255, 255).uv(1, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0, 0, 1).endVertex();
        vc.vertex(pose.pose(), -s,  s, 0).color(255, 255, 255, 255).uv(0, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0, 0, 1).endVertex();

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MatchProjectile match) {
        return texture;
    }
}
