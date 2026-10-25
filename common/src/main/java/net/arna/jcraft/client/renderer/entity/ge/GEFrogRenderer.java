package net.arna.jcraft.client.renderer.entity.ge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renderer for {@link net.arna.jcraft.common.entity.GEFrogEntity GEFrogEntity}.
 */
@Environment(EnvType.CLIENT)
public class GEFrogRenderer extends FrogRenderer {
    private final ItemInHandRenderer heldItemRenderer;

    public GEFrogRenderer(final EntityRendererProvider.Context context) {
        super(context);
        this.heldItemRenderer = context.getItemInHandRenderer();
    }

    @Override
    public void render(final Frog frogEntity, final float f, final float g, final PoseStack matrixStack, final @NonNull MultiBufferSource vertexConsumerProvider, final int i) {
        matrixStack.pushPose();
        matrixStack.translate(0.0, 0.4, 0);
        matrixStack.mulPose(Axis.YN.rotationDegrees(frogEntity.getYRot()));
        final ItemStack itemStack = frogEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        this.heldItemRenderer.renderItem(frogEntity, itemStack, ItemDisplayContext.GROUND, false, matrixStack, vertexConsumerProvider, i);
        matrixStack.popPose();
        super.render(frogEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
