package net.arna.jcraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FrogEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class GEFrogRenderer extends FrogEntityRenderer {
    private final HeldItemRenderer heldItemRenderer;
    public GEFrogRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.heldItemRenderer = context.getHeldItemRenderer();
    }

    @Override
    public void render(FrogEntity frogEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0, 0.4, 0);
        matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(frogEntity.getYaw()));
        ItemStack itemStack = frogEntity.getEquippedStack(EquipmentSlot.MAINHAND);
        this.heldItemRenderer.renderItem(frogEntity, itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i);
        matrixStack.pop();
        super.render(frogEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
