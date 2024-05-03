package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Collections;

public class StandEntityRenderer<T extends StandEntity<?, ?>> extends GeoEntityRenderer<T> {

    protected StandEntityRenderer(EntityRendererFactory.Context renderManager, GeoModel<T> modelProvider) {
        super(renderManager, modelProvider);
    }

    /*
    Cutout - no alpha
    CutoutNoCull - identical (copium)
    Alpha - no lighting
    Translucent - with alpha, nothing renders through
    Decal - invisible
    NoOutline - transparent, everything is visible through
    Shadow - inverted normals, no alpha
    SmoothCutout - Cutout
    Solid - no transparency
     */
    public static RenderLayer renderTypeOf(StandEntity<?, ?> stand, Identifier textureLocation) {
        MinecraftClient mcClient = MinecraftClient.getInstance();
        return mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null && JUtils.getStand(mcClient.player) == stand ?
                RenderLayer.getEntityNoOutline(textureLocation) : RenderLayer.getEntityTranslucent(textureLocation);
    }

    @Override
    public RenderLayer getRenderType(T animatable, Identifier texture, @Nullable VertexConsumerProvider bufferSource, float partialTick) {
        MinecraftClient mcClient = MinecraftClient.getInstance();
        return mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null && JUtils.getStand(mcClient.player) == animatable ?
                RenderLayer.getEntityNoOutline(texture) : RenderLayer.getEntityTranslucent(texture);
    }

    // Adds the ability to change render alpha
    @Override
    public void preRender(MatrixStack poseStack, T stand, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        float a = getAlpha(stand, partialTick);
        a *= alpha;
        if (a <= 0.01f) return;

        super.preRender(poseStack, stand, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, getRed(stand, red, a), getGreen(stand, green, a), getBlue(stand, blue, a), alpha);
    }

    // Better than a mixin, makes stands look towards the users HEAD rotation as opposed to body
    @Override
    public void render(T animatable, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource,
                       int packedLight) {
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
        if (!stand.hasUser()) return super.getBlockLight(stand, pos);

        if (stand.isOnFire() || stand.getUserOrThrow().isOnFire()) return 15;
        return stand.getWorld().getLightLevel(LightType.BLOCK, stand.getUserOrThrow().getBlockPos());
    }

    @Override
    protected int getSkyLight(T stand, BlockPos pos) {
        return stand.hasUser() ? stand.getWorld().getLightLevel(LightType.SKY, stand.getUserOrThrow().getBlockPos()) :
                super.getSkyLight(stand, pos);
    }

    public static boolean shouldApplyAlpha(StandEntity<?, ?> stand) {
        MinecraftClient mcClient = MinecraftClient.getInstance();
        return mcClient.player != null && mcClient.options.getPerspective().isFirstPerson() && JUtils.getStand(mcClient.player) == stand;
    }

    public static float getAlpha(StandEntity<?, ?> stand, float tickDelta) {
        if (!shouldApplyAlpha(stand)) return 1f;

        // If we have an alpha override this tick and had one last tick too, just use that.
        if (stand.hasAlphaOverride() && stand.getPrevAlpha() >= 0) return stand.getAlphaOverride();

        float a = MathHelper.clamp((float) stand.squaredDistanceTo(MinecraftClient.getInstance().player) / 2f, 0, 1);
        if (!stand.hasAlphaOverride()) return a; // If we don't have an override, use this alpha value.

        // If we do have an override, but didn't last tick, lerp between the previous alpha and the override.
        return MathHelper.lerp(tickDelta, a, stand.getAlphaOverride());
    }

    protected float getRed(T stand, float red, float alpha) {
        return red;
    }

    protected float getGreen(T stand, float green, float alpha) {
        return green;
    }

    protected float getBlue(T stand, float blue, float alpha) {
        return blue;
    }
}
