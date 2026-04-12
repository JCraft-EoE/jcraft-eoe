package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * The {@link StandEntityRenderer} for {@link D4CEntity}.
 */
@Environment(EnvType.CLIENT)
public class D4CRenderer extends StandEntityRenderer<D4CEntity> {

    /*
        /execute as @e[type=jcraft:d4c] run data merge entity @s {HandItems:[{id:"jcraft:fv_revolver", Count:1b},{id:"jcraft:fv_revolver", Count:1b}]}
     */
    protected float partialTick = 0f;

    public D4CRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b
                .addRenderLayer(new D4CHandItemsRenderLayer())
                , JStandTypeRegistry.D4C.get(), 0, 0);
    }

    private static class D4CHandItemsRenderLayer extends HandItemsRenderLayer<D4CEntity> {
        // Do some quick render modifications depending on what the item is
        @Override
        protected void renderItemForBone(final AzRendererPipelineContext<UUID, D4CEntity> context, final AzBone bone, final ItemStack stack, final D4CEntity animatable) {
            final var poseStack = context.poseStack();

            poseStack.mulPose(Axis.XP.rotationDegrees(bone.getRotX() * 57.29578f));
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            // poseStack.mulPose(Axis.ZP.rotationDegrees(180f));

            super.renderItemForBone(context, bone, stack, animatable);
        }
    }

    /*
    @Override
    public void actuallyRender(final PoseStack poseStack, final D4CEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, final boolean isReRender, final float partialTick, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        final float a = StandEntityRenderer.getAlpha(animatable, partialTick);
        final float gR = 1.0f - a;

        this.mainHandItem = animatable.getMainHandItem();
        this.offHandItem = animatable.getOffhandItem();

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green - gR, blue, a);
    }*/
}
