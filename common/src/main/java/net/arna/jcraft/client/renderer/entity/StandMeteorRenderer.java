package net.arna.jcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.model.entity.StandMeteorModel;
import net.arna.jcraft.common.entity.StandMeteorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class StandMeteorRenderer extends MobRenderer<StandMeteorEntity, StandMeteorModel> {
    public static final ModelLayerLocation STAND_METEOR_LAYER = new ModelLayerLocation(JCraft.id("stand_meteor"), "main");
    public StandMeteorRenderer(final EntityRendererProvider.Context context) {
        super(context, new StandMeteorModel(context.bakeLayer(STAND_METEOR_LAYER)), 0.2F);
    }

    public void render(@NonNull final StandMeteorEntity entity, final float entityYaw, final float partialTicks,
                       @NonNull final PoseStack poseStack, @NonNull final MultiBufferSource buffer, final int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.tickCount + partialTicks));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount + partialTicks));
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(@NonNull final StandMeteorEntity entity) {
        return JCraft.id("textures/entity/stand_meteor.png");
    }
}
