package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.AbstractRenderLayer;
import net.arna.jcraft.client.renderer.entity.layer.SCRapierLayer;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * The {@link StandEntityRenderer} for {@link SilverChariotEntity}.
 */
@Environment(EnvType.CLIENT)
public class SilverChariotRenderer extends StandEntityRenderer<SilverChariotEntity> {
    public SilverChariotRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b
                        .addRenderLayer(new SCAfterimageLayer())
                        .addRenderLayer(new SCRapierLayer()),
                /*
                entity -> JCraft.id(MODEL_STR_TEMPLATE.formatted(JStandTypeRegistry.SILVER_CHARIOT.get().getId().getPath())),
                entity -> switch(entity.getMode()) {
                    case ARMORLESS -> NO_ARMOR_TEXTURE;
                    case POSSESSED -> POSSESSED_TEXTURE;
                    default -> StandEntityRenderer.getTextureLocation(entity);
                },
                 */
                JStandTypeRegistry.SILVER_CHARIOT.get(), false, false, 0f, 0f, 90f);
    }

    private static class SCAfterimageLayer extends AbstractRenderLayer<SilverChariotEntity> {
        public void render(final @NonNull AzRendererPipelineContext<UUID, SilverChariotEntity> pc) {
            final var animatable = pc.animatable();
            // final var partialTick = pc.partialTick();
            final var poseStack = pc.poseStack();

            // final float a = StandEntityRenderer.getAlpha(animatable, partialTick);

            if (animatable.getMode() == SilverChariotEntity.Mode.ARMORLESS && animatable.hasUser()) {
                final Vec3 velocity = JUtils.deltaPos(animatable);
                final LivingEntity user = animatable.getUser();

                if (user == null) return;

                for (int i = 0; i < 3; i++) {
                    final float offsetScale = -(i + 1) / 2.5f;

                    double y = velocity.y;
                    if (-0.2 < -y && y < 0.2) {
                        y = 0;
                    }

                    poseStack.pushPose();
                    poseStack.translate(velocity.x * offsetScale, y * offsetScale, velocity.z * offsetScale);
                    pc.setAlpha((3 - i) / 3f);
                    pc.rendererPipeline().reRender(pc);
                    poseStack.popPose();
                }
            }
        }
    }

    /*
    @Override
    public @NonNull ResourceLocation getTextureLocation(final @NonNull SilverChariotEntity entity) {
        return switch(entity.getMode()) {
            case ARMORLESS ->  NO_ARMOR_TEXTURE;
            case POSSESSED -> POSSESSED_TEXTURE;
            default -> StandEntityRenderer.getTextureLocation(entity);
        };
    }
     */

    // Adds ability to change render alpha

    /*
    @Override
    public void actuallyRender(final PoseStack poseStack, final SilverChariotEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, final boolean isReRender, final float partialTick, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        final float a = StandEntityRenderer.getAlpha(animatable, partialTick);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, a);
        if (animatable.getMode() == SilverChariotEntity.Mode.ARMORLESS && animatable.hasUser()) {
            for (double i = 0; i < 3; i++) {
                renderAfterImage(animatable.getUserOrThrow(), JUtils.deltaPos(animatable).scale(i * 2.0), 1f,
                        model, animatable, partialTick, RenderType.entityNoOutline(getTextureLocation(animatable)),
                        poseStack, bufferSource, buffer, packedLight, packedOverlay, red, green, blue,
                        a);
            }
        }
    }

    private void renderAfterImage(LivingEntity user, Vec3 velocity, float a, BakedGeoModel model, SilverChariotEntity animatable,
                             float partialTicks, RenderType type, PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                             VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStack.pushPose();

        matrixStack.mulPose(Axis.YP.rotationDegrees(user.yBodyRot));

        double y = velocity.y;
        if (-0.2 < -y && y < 0.2) {
            y = 0;
        }

        matrixStack.translate(velocity.x, y, velocity.z);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-user.yBodyRot));
        super.actuallyRender(matrixStack, animatable, model, type, renderTypeBuffer, vertexBuilder, false, partialTicks, packedLightIn, packedOverlayIn, red, green, blue, a);
        matrixStack.popPose();
    }*/
}
