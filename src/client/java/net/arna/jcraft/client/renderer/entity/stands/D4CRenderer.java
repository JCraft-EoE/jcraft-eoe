package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.entity.stand.StandType;
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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.geckolib3.compat.PatchouliCompat;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.util.EModelRenderCycle;

import java.util.Collections;

// TODO: ExtendedStandEntityRenderer<T extends StandEntity> , then convert D4CRenderer and WhiteSnakeRenderer
public class D4CRenderer extends ExtendedGeoEntityRenderer<D4CEntity> {
    protected ItemStack mainHandItem, offHandItem;
    public D4CRenderer(EntityRendererFactory.Context context) {
        super(context, new StandEntityModel<>(StandType.D4C));
    }

    @Override
    public RenderLayer getRenderType(D4CEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        return StandEntityRenderer.renderTypeOf(animatable, textureLocation);
    }

    @Override
    public void render(GeoModel model, D4CEntity animatable, float tickDelta, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = StandEntityRenderer.getAlpha(animatable, tickDelta);
        float gR = 1.0f - a;

        super.render(model, animatable, tickDelta, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green - gR, blue, a);
    }

    @Override
    public void render(D4CEntity animatable, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
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
            //lerpBodyRot = MathHelper.lerpAngleDegrees(partialTick, livingentity.prevBodyYaw, livingentity.bodyYaw);
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

        AnimationEvent<D4CEntity> predicate = new AnimationEvent<D4CEntity>(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -getSwingMotionAnimThreshold() || limbSwingAmount > getSwingMotionAnimThreshold()), Collections.singletonList(entityModelData));
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelResource(animatable));

        this.modelProvider.setCustomAnimations(animatable, getInstanceId(animatable), predicate); // TODO change to setCustomAnimations in 1.20+

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
            for (GeoLayerRenderer<D4CEntity> layerRenderer : this.layerRenderers) {
                renderLayer(poseStack, bufferSource, packedLight, animatable, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                        netHeadYaw, headPitch, bufferSource, layerRenderer);
            }
        }

        if (FabricLoader.getInstance().isModLoaded("patchouli"))
            PatchouliCompat.patchouliLoaded(poseStack);

        poseStack.pop();
    }

    @Override
    protected int getBlockLight(D4CEntity stand, BlockPos pos) {
        if (stand.hasUser()) {
            if (stand.isOnFire() || stand.getUserOrThrow().isOnFire()) return 15;
            return stand.world.getLightLevel(LightType.BLOCK, stand.getUserOrThrow().getBlockPos());
        }
        return super.getBlockLight(stand, pos);
    }

    @Override
    protected int getSkyLight(D4CEntity stand, BlockPos pos) {
        return stand.hasUser() ? stand.world.getLightLevel(LightType.SKY, stand.getUserOrThrow().getBlockPos()) :
                super.getSkyLight(stand, pos);
    }

    /*
        /execute as @e[type=jcraft:d4c] run data merge entity @s {HandItems:[{id:"jcraft:fv_revolver", Count:1b},{id:"jcraft:fv_revolver", Count:1b}]}
     */
    protected float partialTick = 0f;
    @Override
    public void renderEarly(D4CEntity animatable, MatrixStack poseStack, float partialTick, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float partialTicks) {
        this.mainHandItem = animatable.getEquippedStack(EquipmentSlot.MAINHAND);
        this.offHandItem = animatable.getEquippedStack(EquipmentSlot.OFFHAND);
        this.partialTick = partialTick;

        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, partialTicks);
    }

    @Override
    protected ItemStack getHeldItemForBone(String boneName, D4CEntity currentEntity) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? mainHandItem : offHandItem;
            case DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? offHandItem : mainHandItem;
            default -> null;
        };
    }

    @Override
    protected ModelTransformation.Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT, DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND; // Do Defaults
            default -> ModelTransformation.Mode.NONE;
        };
    }

    @Override
    protected void preRenderItem(MatrixStack stack, ItemStack item, String boneName, D4CEntity currentEntity, IBone bone) {
        //todo: fix d4c revolver rotation; a hack is currently implemented due to something (sodium?) breaking hand rotation for d4c
        float ang = -90f;
        D4CEntity.State state = currentEntity.getState();
        if (state == D4CEntity.State.THROW || state == D4CEntity.State.GIVE_GUN)
            ang += (currentEntity.getMoveStun() + 1f - this.partialTick) * 65f;
        stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(ang));
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @Override
    protected Identifier getTextureForBone(String boneName, D4CEntity currentEntity) {
        return null;
    }

    @Override
    protected BlockState getHeldBlockForBone(String boneName, D4CEntity currentEntity) {
        return null;
    }

    @Override
    protected void postRenderItem(MatrixStack PoseStack, ItemStack item, String boneName, D4CEntity currentEntity, IBone bone) {
    }

    @Override
    protected void preRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, D4CEntity currentEntity) {
    }

    @Override
    protected void postRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, D4CEntity currentEntity) {
    }
}
