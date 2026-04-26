package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.data.EntityModelData;
import net.arna.jcraft.client.model.entity.stand.TCBModel;
import net.arna.jcraft.common.entity.stand.TCBEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;



//I HATE THIS SO MUCHHHH ARRRGHGHGHHGHHG
public class TCBRenderer extends StandEntityRenderer<TCBEntity> {
    public TCBRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TCBModel());
    }

    @Override
    public RenderType getRenderType(TCBEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, TCBEntity animatable, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                               float partialTick, int packedLight, int packedOverlay, float red, float green,
                               float blue, float alpha) {
        if (!animatable.hasUser() || !(animatable.getUserOrThrow() instanceof Player player)) {
            return;
        }

        poseStack.pushPose();

        // Calculate position offsets based on player state
        float yOffset = 0;
        float zOffset = 0;

        if (player.isCrouching()) {
            yOffset = -0.1875f; // 3 pixels down
            zOffset = 0.0625f;  // 1 pixel forward
        }

        // Apply the offset
        poseStack.translate(0, yOffset, zOffset);

        if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(clientPlayer);
            PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();

            // Setup player model animations
            float limbSwingAmount = player.walkAnimation.speed(partialTick);
            float limbSwing = player.walkAnimation.position(partialTick);
            float ageInTicks = player.tickCount + partialTick;
            float headYaw = Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot) - Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
            float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

            // Set crouching state
            playerModel.crouching = player.isCrouching();

            playerModel.setupAnim(clientPlayer, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
            playerModel.prepareMobModel(clientPlayer, limbSwing, limbSwingAmount, partialTick);

            // Get bones for player animation copying
            GeoBone head = model.getBone("bipedHead").orElse(null);
            GeoBone body = model.getBone("bipedBody").orElse(null);
            GeoBone rightArm = model.getBone("bipedRightArm").orElse(null);
            GeoBone leftArm = model.getBone("bipedLeftArm").orElse(null);
            GeoBone rightLeg = model.getBone("bipedRightLeg").orElse(null);
            GeoBone leftLeg = model.getBone("bipedLeftLeg").orElse(null);

            // Detect animation type
            boolean isPlayerAnimatorAnim = Math.abs(playerModel.leftLeg.xRot) > 0.8f ||
                    Math.abs(playerModel.leftLeg.yRot) > 0.1f ||
                    Math.abs(playerModel.leftLeg.zRot) > 0.1f ||
                    Math.abs(playerModel.rightLeg.xRot) > 0.8f ||
                    Math.abs(playerModel.rightLeg.yRot) > 0.1f ||
                    Math.abs(playerModel.rightLeg.zRot) > 0.1f;

            // Store original positions from animations (they will be applied by handleAnimations)
            float headPosX = head != null ? head.getPosX() : 0;
            float headPosY = head != null ? head.getPosY() : 0;
            float headPosZ = head != null ? head.getPosZ() : 0;

            float bodyPosX = body != null ? body.getPosX() : 0;
            float bodyPosY = body != null ? body.getPosY() : 0;
            float bodyPosZ = body != null ? body.getPosZ() : 0;

            float rightArmPosX = rightArm != null ? rightArm.getPosX() : 0;
            float rightArmPosY = rightArm != null ? rightArm.getPosY() : 0;
            float rightArmPosZ = rightArm != null ? rightArm.getPosZ() : 0;

            float leftArmPosX = leftArm != null ? leftArm.getPosX() : 0;
            float leftArmPosY = leftArm != null ? leftArm.getPosY() : 0;
            float leftArmPosZ = leftArm != null ? leftArm.getPosZ() : 0;

            float rightLegPosX = rightLeg != null ? rightLeg.getPosX() : 0;
            float rightLegPosY = rightLeg != null ? rightLeg.getPosY() : 0;
            float rightLegPosZ = rightLeg != null ? rightLeg.getPosZ() : 0;

            float leftLegPosX = leftLeg != null ? leftLeg.getPosX() : 0;
            float leftLegPosY = leftLeg != null ? leftLeg.getPosY() : 0;
            float leftLegPosZ = leftLeg != null ? leftLeg.getPosZ() : 0;

            // Handle animations
            if (!isReRender) {
                AnimationState<TCBEntity> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
                animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
                animationState.setData(DataTickets.ENTITY, animatable);
                animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(false, false, 0, 0));

                long instanceId = getInstanceId(animatable);
                this.model.addAdditionalStateData(animatable, instanceId, animationState::setData);
                this.model.handleAnimations(animatable, instanceId, animationState);
            }

            // Now add back the stored positions (animations may have reset them)
            if (head != null) {
                head.setPosX(head.getPosX() + headPosX);
                head.setPosY(head.getPosY() + headPosY - 1f); // -1 offset for head because yes
                head.setPosZ(head.getPosZ() + headPosZ);

                head.setRotX(-playerModel.head.xRot);
                head.setRotY(-playerModel.head.yRot + (float)Math.PI);
                head.setRotZ(-playerModel.head.zRot);
            }

            if (body != null) {
                body.setPosX(body.getPosX() + bodyPosX);
                body.setPosY(body.getPosY() + bodyPosY);
                body.setPosZ(body.getPosZ() + bodyPosZ);

                body.setRotX(-playerModel.body.xRot);
                body.setRotY(playerModel.body.yRot + (float)Math.PI);
                body.setRotZ(-playerModel.body.zRot);
            }

            if (rightArm != null) {
                rightArm.setPosX(rightArm.getPosX() + rightArmPosX);
                rightArm.setPosY(rightArm.getPosY() + rightArmPosY);
                rightArm.setPosZ(rightArm.getPosZ() + rightArmPosZ);

                rightArm.setRotX(playerModel.leftArm.xRot);
                rightArm.setRotY(-playerModel.leftArm.yRot);
                rightArm.setRotZ(-playerModel.leftArm.zRot);
            }

            if (leftArm != null) {
                leftArm.setPosX(leftArm.getPosX() + leftArmPosX);
                leftArm.setPosY(leftArm.getPosY() + leftArmPosY);
                leftArm.setPosZ(leftArm.getPosZ() + leftArmPosZ);

                leftArm.setRotX(playerModel.rightArm.xRot);
                leftArm.setRotY(-playerModel.rightArm.yRot);
                leftArm.setRotZ(-playerModel.rightArm.zRot);
            }

            if (rightLeg != null) {
                rightLeg.setPosX(rightLeg.getPosX() + rightLegPosX);
                rightLeg.setPosY(rightLeg.getPosY() + rightLegPosY);
                rightLeg.setPosZ(rightLeg.getPosZ() + rightLegPosZ);

                if (isPlayerAnimatorAnim) {
                    rightLeg.setRotX(playerModel.rightLeg.xRot);
                    rightLeg.setRotY(-playerModel.rightLeg.yRot);
                    rightLeg.setRotZ(playerModel.rightLeg.zRot);
                } else {
                    rightLeg.setRotX(-playerModel.rightLeg.xRot);
                    rightLeg.setRotY(-playerModel.rightLeg.yRot);
                    rightLeg.setRotZ(-playerModel.rightLeg.zRot);
                }
            }

            if (leftLeg != null) {
                leftLeg.setPosX(leftLeg.getPosX() + leftLegPosX);
                leftLeg.setPosY(leftLeg.getPosY() + leftLegPosY);
                leftLeg.setPosZ(leftLeg.getPosZ() + leftLegPosZ);

                if (isPlayerAnimatorAnim) {
                    leftLeg.setRotX(playerModel.leftLeg.xRot);
                    leftLeg.setRotY(-playerModel.leftLeg.yRot);
                    leftLeg.setRotZ(playerModel.leftLeg.zRot);
                } else {
                    leftLeg.setRotX(-playerModel.leftLeg.xRot);
                    leftLeg.setRotY(-playerModel.leftLeg.yRot);
                    leftLeg.setRotZ(-playerModel.leftLeg.zRot);
                }
            }
        }

        // Render
        float renderAlpha = shouldApplyAlpha(animatable) ? alpha * getAlpha(animatable, partialTick) : alpha;

        updateAnimatedTextureFrame(animatable);
        for (GeoBone bone : model.topLevelBones()) {
            renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, red, green, blue, renderAlpha);
        }

        poseStack.popPose();
    }

    @Override
    public void preRender(PoseStack poseStack, TCBEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, float red, float green, float blue, float alpha) {
        if (!animatable.hasUser()) {
            return;
        }

        LivingEntity user = animatable.getUserOrThrow();

        // Position at player
        double x = Mth.lerp(partialTick, user.xo, user.getX()) - Mth.lerp(partialTick, animatable.xo, animatable.getX());
        double y = Mth.lerp(partialTick, user.yo, user.getY()) - Mth.lerp(partialTick, animatable.yo, animatable.getY());
        double z = Mth.lerp(partialTick, user.zo, user.getZ()) - Mth.lerp(partialTick, animatable.zo, animatable.getZ());

        poseStack.translate(x, y, z);

        // Match player rotation
        float bodyRot = Mth.rotLerp(partialTick, user.yBodyRotO, user.yBodyRot);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-bodyRot));

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    protected void applyRotations(TCBEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                  float partialTick, float nativeScale) {
        // Skip - handled in preRender
    }
}