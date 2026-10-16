package net.arna.jcraft.mixin.client.gravity;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FishingHookRenderer.class)
public abstract class FishingBobberEntityRendererMixin extends EntityRenderer<FishingHook> {

    @Shadow
    @Final
    private static RenderType RENDER_TYPE;

    @Shadow
    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
    }

    @Shadow
    private static void stringVertex(float x, float y, float z, VertexConsumer buffer, PoseStack.Pose normal, float f, float g) {
    }

    @Shadow
    private static float fraction(int value, int max) {
        return 0f;
    }

    protected FishingBobberEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @WrapMethod(method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void jcraft$render(final FishingHook entity, final float entityYaw, final float partialTicks, final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight, final Operation<Void> original) {
        Player playerEntity = entity.getPlayerOwner();
        if (playerEntity == null) {
            original.call(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        Direction gravityDirection = GravityChangerAPI.getGravityDirection(playerEntity);
        if (gravityDirection == Direction.DOWN) {
            original.call(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        PoseStack.Pose entry = poseStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0f, 0, 0, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1f, 0, 1, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1f, 1, 1, 0);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0f, 1, 0, 0);
        poseStack.popPose();
        int armOffset = playerEntity.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemStack = playerEntity.getMainHandItem();
        if (!itemStack.is(Items.FISHING_ROD)) {
            armOffset = -armOffset;
        }

        float handSwingProgress = playerEntity.getAttackAnim(partialTicks);
        float sinHandSwingProgress = Mth.sin(Mth.sqrt(handSwingProgress) * 3.1415927f);
        float radBodyYaw = Mth.lerp(partialTicks, playerEntity.yBodyRotO, playerEntity.yBodyRot) * 0.017453292f;
        double sinBodyYaw = Mth.sin(radBodyYaw);
        double cosBodyYaw = Mth.cos(radBodyYaw);
        double scaledArmOffset = (double)armOffset * 0.35;
        Vec3 lineStart;
        if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && playerEntity == Minecraft.getInstance().player) {
            Vec3 lineOffset = RotationUtil.vecWorldToPlayer(this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) armOffset * 0.525f, -0.1f), gravityDirection);
            lineOffset = lineOffset.scale(960d / this.entityRenderDispatcher.options.fov().get());
            lineOffset = lineOffset.yRot(sinHandSwingProgress * 0.5f);
            lineOffset = lineOffset.xRot(-sinHandSwingProgress * 0.7f);
            lineStart = new Vec3(
                    Mth.lerp(partialTicks, playerEntity.xo, playerEntity.getX()),
                    Mth.lerp(partialTicks, playerEntity.yo, playerEntity.getY()),
                    Mth.lerp(partialTicks, playerEntity.zo, playerEntity.getZ())
            ).add(RotationUtil.vecPlayerToWorld(lineOffset.add(0d, playerEntity.getEyeHeight(), 0d), gravityDirection));
        } else {
            lineStart = new Vec3(
                    Mth.lerp(partialTicks, playerEntity.xo, playerEntity.getX()),
                    playerEntity.yo + (playerEntity.getY() - playerEntity.yo) * partialTicks,
                    Mth.lerp(partialTicks, playerEntity.zo, playerEntity.getZ())
            ).add(RotationUtil.vecPlayerToWorld(
                    -cosBodyYaw * scaledArmOffset - sinBodyYaw * 0.8,
                    playerEntity.getEyeHeight() + (playerEntity.isCrouching() ? -0.1875 : 0d) - 0.45,
                    -sinBodyYaw * scaledArmOffset + cosBodyYaw * 0.8,
                    gravityDirection
            ));
        }

        double bobberX = Mth.lerp(partialTicks, entity.xo, entity.getX());
        double bobberY = Mth.lerp(partialTicks, entity.yo, entity.getY()) + 0.25;
        double bobberZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        float relX = (float) (lineStart.x - bobberX);
        float relY = (float) (lineStart.y - bobberY);
        float relZ = (float) (lineStart.z - bobberZ);
        VertexConsumer vertexConsumer2 = buffer.getBuffer(RenderType.lineStrip());
        PoseStack.Pose entry2 = poseStack.last();

        for (int i = 0; i <= 16; ++i) {
            stringVertex(relX, relY, relZ, vertexConsumer2, entry2, fraction(i, 16), fraction(i + 1, 16));
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

}
