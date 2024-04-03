package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.WhiteSnakeModel;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class WhiteSnakeRenderer extends ExtendedStandEntityRenderer<WhiteSnakeEntity> {
    public WhiteSnakeRenderer(EntityRendererFactory.Context context) {
        super(context, new WhiteSnakeModel());
    }

    @Override
    public RenderLayer getRenderType(WhiteSnakeEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        return StandEntityRenderer.renderTypeOf(animatable, textureLocation);
    }

    @Override
    public void renderEarly(WhiteSnakeEntity animatable, MatrixStack poseStack, float partialTick, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float partialTicks) {
        this.mainHandItem = animatable.getEquippedStack(EquipmentSlot.MAINHAND);
        this.offHandItem = animatable.getEquippedStack(EquipmentSlot.OFFHAND);
        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, partialTicks);
    }

    @Override
    public void render(GeoModel model, WhiteSnakeEntity animatable, float tickDelta, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = StandEntityRenderer.getAlpha(animatable, tickDelta);
        super.render(model, animatable, tickDelta, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
    }

    @Override
    protected ItemStack getHeldItemForBone(String boneName, WhiteSnakeEntity currentEntity) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? mainHandItem : offHandItem;
            case DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? offHandItem : mainHandItem;
            default -> null;
        };
    }

    @Nullable
    @Override
    protected ModelTransformation.Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT, DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND; // Do Defaults
            default -> ModelTransformation.Mode.NONE;
        };
    }

    @Override
    protected void preRenderItem(MatrixStack stack, ItemStack item, String boneName, WhiteSnakeEntity currentEntity, IBone bone) {
        stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(bone.getRotationX() - 90f));
        stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(bone.getRotationY() - 90f));
        stack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(bone.getRotationZ()));
    }
}
