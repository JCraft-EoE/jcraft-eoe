package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link StandEntityRenderer} for {@link AerosmithEntity}.
 */
@Environment(EnvType.CLIENT)
public class AerosmithRenderer extends StandEntityRenderer<AerosmithEntity> {
    protected final ItemRenderer itemRenderer;

    public AerosmithRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.AEROSMITH.get());
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(@NotNull final AerosmithEntity entity, final float entityYaw, final float partialTick, @NotNull final PoseStack poseStack, @NotNull final MultiBufferSource bufferSource, final int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        if (entity.getHoldItem() != null) {
            poseStack.pushPose();
            poseStack.translate(0f, -1f, 0f);
            itemRenderer.renderStatic(entity.getHoldItem(), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), entity.getId());
            poseStack.popPose();
        }
    }
}
