package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Collections;
import java.util.List;

public class ExtendedStandEntityRenderer<T extends StandEntity<?,?>> extends DynamicGeoEntityRenderer<T> {
    protected ItemStack mainHandItem, offHandItem;
    protected ExtendedStandEntityRenderer(EntityRendererFactory.Context renderManager, GeoModel<T> modelProvider) {
        super(renderManager, modelProvider);
    }

    @Override
    public RenderLayer getRenderType(T animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        return StandEntityRenderer.renderTypeOf(animatable, textureLocation);
    }

    @Override
    public void render(T animatable, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        poseStack.push();

        Entity leashHolder = animatable.getHoldingEntity();
        if (leashHolder != null) renderLeash(animatable, partialTick, poseStack, bufferSource, leashHolder);

        this.dispatchedMat = poseStack.peek().getPositionMatrix().copy();
        boolean shouldSit = animatable.hasVehicle() && (animatable.getVehicle() != null);
        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = shouldSit;
        entityModelData.isChild = animatable.isBaby();

        float lerpBodyRot = MathHelper.lerpAngleDegrees(partialTick, animatable.prevBodyYaw, animatable.bodyYaw);
        float lerpHeadRot = MathHelper.lerpAngleDegrees(partialTick, animatable.prevHeadYaw, animatable.headYaw);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && !animatable.isFree() && animatable.getVehicle() instanceof LivingEntity livingentity) {
            lerpBodyRot = MathHelper.lerpAngleDegrees(partialTick, livingentity.prevHeadYaw, livingentity.headYaw);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;

            if (clampedHeadYaw * clampedHeadYaw > 2500f)
                lerpBodyRot += clampedHeadYaw * 0.2f;

            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

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

        if (!shouldSit && animatable.isAlive()) {
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

        AnimationEvent<T> predicate = new AnimationEvent<T>(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -getSwingMotionAnimThreshold() || limbSwingAmount > getSwingMotionAnimThreshold()), Collections.singletonList(entityModelData));
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelResource(animatable));

        this.modelProvider.setCustomAnimations(animatable, getInstanceId(animatable), predicate);

        poseStack.translate(0, 0.01f, 0);
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

        if (!animatable.isSpectator()) {
            for (GeoLayerRenderer<T> layerRenderer : this.layerRenderers) {
                renderLayer(poseStack, bufferSource, packedLight, animatable, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                        netHeadYaw, headPitch, bufferSource, layerRenderer);
            }
        }

        if (FabricLoader.getInstance().isModLoaded("patchouli"))
            PatchouliCompat.patchouliLoaded(poseStack);

        poseStack.pop();
    }

    @Override
    protected int getBlockLight(T stand, BlockPos pos) {
        if (stand.hasUser()) {
            if (stand.isOnFire() || stand.getUserOrThrow().isOnFire()) return 15;
            return stand.world.getLightLevel(LightType.BLOCK, stand.getUserOrThrow().getBlockPos());
        }
        return super.getBlockLight(stand, pos);
    }

    @Override
    protected int getSkyLight(T stand, BlockPos pos) {
        return stand.hasUser() ? stand.world.getLightLevel(LightType.SKY, stand.getUserOrThrow().getBlockPos()) :
                super.getSkyLight(stand, pos);
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @Nullable
    @Override
    protected Identifier getTextureForBone(String boneName, T currentEntity) {
        return null;
    }

    @Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, T currentEntity) {
        return null;
    }

    @Override
    protected ModelTransformation.Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return null;
    }

    @Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, T currentEntity) {
        return null;
    }

    @Override
    protected void preRenderItem(MatrixStack PoseStack, ItemStack item, String boneName, T currentEntity, IBone bone) {

    }

    @Override
    protected void preRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, T currentEntity) {

    }

    @Override
    protected void postRenderItem(MatrixStack PoseStack, ItemStack item, String boneName, T currentEntity, IBone bone) {

    }

    @Override
    protected void postRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, T currentEntity) {

    }
}
