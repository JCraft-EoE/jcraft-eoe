package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GESnakeModel;
import net.arna.jcraft.common.entity.GESnakeEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;

import java.util.Objects;

public class GESnakeRenderer extends DynamicGeoEntityRenderer<GESnakeEntity> {
    protected ItemStack mainHandItem;

    public GESnakeRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GESnakeModel());
    }

    @Override
    public void renderEarly(GESnakeEntity animatable, MatrixStack poseStack, float partialTick, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float partialTicks) {
        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, partialTicks);
        this.mainHandItem = animatable.getEquippedStack(EquipmentSlot.MAINHAND);
    }

    @Override
    protected ItemStack getHeldItemForBone(String boneName, GESnakeEntity currentEntity) {
        return Objects.equals(boneName, "body") ? mainHandItem : null;
    }

    @Override
    protected ModelTransformation.Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return ModelTransformation.Mode.NONE;
    }

    @Override
    protected void preRenderItem(MatrixStack stack, ItemStack item, String boneName, GESnakeEntity currentEntity, IBone bone) {
        if (item == this.mainHandItem) {
            stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90f));
        }
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @Override
    protected Identifier getTextureForBone(String boneName, GESnakeEntity currentEntity) {
        return null;
    }

    @Override
    protected BlockState getHeldBlockForBone(String boneName, GESnakeEntity currentEntity) {
        return null;
    }

    @Override
    protected void postRenderItem(MatrixStack PoseStack, ItemStack item, String boneName, GESnakeEntity currentEntity, IBone bone) {
    }

    @Override
    protected void preRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, GESnakeEntity currentEntity) {
    }

    @Override
    protected void postRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, GESnakeEntity currentEntity) {
    }
}
