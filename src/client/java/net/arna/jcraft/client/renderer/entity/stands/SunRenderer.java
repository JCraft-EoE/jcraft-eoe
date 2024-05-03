package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.client.model.entity.TheSunModel;
import net.arna.jcraft.client.renderer.entity.layer.SunGlowLayer;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Collections;

public class SunRenderer extends GeoEntityRenderer<TheSunEntity> {
    public SunRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TheSunModel());
        addRenderLayer(new SunGlowLayer(this));
    }

    @Override
    protected int getBlockLight(TheSunEntity entity, BlockPos pos) { return 15; }

    @Override
    protected int getSkyLight(TheSunEntity entity, BlockPos pos) { return 15; }

    //TODO: translucent layer that isn't layered over and has no shading
    @Override
    public RenderLayer getRenderType(TheSunEntity animatable, float partialTick, MatrixStack poseStack, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, int packedLight, Identifier texture) {
        return RenderLayer.getEntityAlpha(texture);
    }

    // Not inlined for sake of debugging
    private static float lerpScale(TheSunEntity animatable, float partialTick) {
        float scale = MathHelper.lerp(partialTick, animatable.prevScale, animatable.getScale());
        return scale;
    }

    @Override
    public void render(TheSunEntity animatable, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        packedLight = 255;

        setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        poseStack.push();

        float scale = lerpScale(animatable, partialTick);
        poseStack.scale(scale, scale, scale);

        this.dispatchedMat = poseStack.peek().getPositionMatrix().copy();
        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = false;
        entityModelData.isChild = animatable.isBaby();

        float lerpBodyRot = MathHelper.lerpAngleDegrees(partialTick, animatable.prevBodyYaw, animatable.bodyYaw);
        float lerpHeadRot = MathHelper.lerpAngleDegrees(partialTick, animatable.prevHeadYaw, animatable.headYaw);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (animatable.getPose() == EntityPose.SLEEPING) {
            Direction bedDirection = animatable.getSleepingDirection();

            if (bedDirection != null) {
                float eyePosOffset = animatable.getEyeHeight(EntityPose.STANDING) - 0.1F;

                poseStack.translate(-bedDirection.getOffsetX() * eyePosOffset, 0, -bedDirection.getOffsetZ() * eyePosOffset);
            }
        }

        float ageInTicks = animatable.age + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick);

        if (animatable.isAlive()) {
            limbSwingAmount = MathHelper.lerp(partialTick, animatable.lastLimbDistance, animatable.limbDistance);
            limbSwing = animatable.limbAngle - animatable.limbDistance * (1 - partialTick);

            if (animatable.isBaby())
                limbSwing *= 3f;

            if (limbSwingAmount > 1f)
                limbSwingAmount = 1f;
        }

        float headPitch = MathHelper.lerp(partialTick, animatable.prevPitch, animatable.getPitch());
        entityModelData.headPitch = -headPitch;
        entityModelData.netHeadYaw = -netHeadYaw;

        AnimationEvent<TheSunEntity> predicate = new AnimationEvent<>(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -getSwingMotionAnimThreshold() || limbSwingAmount > getSwingMotionAnimThreshold()), Collections.singletonList(entityModelData));
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelResource(animatable));

        this.modelProvider.setCustomAnimations(animatable, getInstanceId(animatable), predicate);

        //poseStack.translate(0, 0.01f, 0);
        RenderSystem.setShaderTexture(0, getTextureLocation(animatable));

        Color renderColor = getRenderColor(animatable, partialTick, poseStack, bufferSource, null, packedLight);
        RenderLayer renderType = getRenderType(animatable, partialTick, poseStack, bufferSource, null, packedLight,
                getTextureLocation(animatable));

        if (!animatable.isInvisibleTo(MinecraftClient.getInstance().player)) {
            VertexConsumer glintBuffer = bufferSource.getBuffer(RenderLayer.getDirectEntityGlint());
            VertexConsumer translucentBuffer = bufferSource
                    .getBuffer(RenderLayer.getEntityTranslucentCull(getTextureLocation(animatable)));

            render(model, animatable, partialTick, renderType, poseStack, bufferSource,
                    glintBuffer != translucentBuffer ? VertexConsumers.union(glintBuffer, translucentBuffer)
                            : null,
                    packedLight, getOverlay(animatable, 0), renderColor.getRed() / 255f,
                    renderColor.getGreen() / 255f, renderColor.getBlue() / 255f,
                    renderColor.getAlpha() / 255f);
        }

        //if (!animatable.isSpectator()) {
            for (GeoLayerRenderer<TheSunEntity> layerRenderer : this.layerRenderers) {
                renderLayer(poseStack, bufferSource, packedLight, animatable, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                        netHeadYaw, headPitch, bufferSource, layerRenderer);
            }
        //}

        if (FabricLoader.getInstance().isModLoaded("patchouli"))
            PatchouliCompat.patchouliLoaded(poseStack);

        poseStack.pop();
    }
}
