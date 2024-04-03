package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;

public class D4CRenderer extends ExtendedStandEntityRenderer<D4CEntity> {
    public D4CRenderer(EntityRendererFactory.Context context) {
        super(context, new StandEntityModel<>(StandType.D4C));
    }

    @Override
    public void render(GeoModel model, D4CEntity animatable, float tickDelta, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = StandEntityRenderer.getAlpha(animatable, tickDelta);
        float gR = 1.0f - a;
        super.render(model, animatable, tickDelta, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green - gR, blue, a);
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
        stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(ang));
    }
}
