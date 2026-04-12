package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.MRGlowLayer;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link MagiciansRedEntity}.
 */
public class MagiciansRedRenderer extends StandEntityRenderer<MagiciansRedEntity> {
    public MagiciansRedRenderer(final EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new MRGlowLayer()), JStandTypeRegistry.MAGICIANS_RED.get(), -0.10f, -0.05f);
    }

    /*@Override
    public void actuallyRender(final PoseStack poseStack, final MagiciansRedEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, final boolean isReRender, final float partialTick, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, StandEntityRenderer.getAlpha(animatable, partialTick));

        if (animatable.getState() == MagiciansRedEntity.State.RED_BIND) {
            if (Minecraft.getInstance().isPaused()) {
                return;
            }
            model.getBone("rope3").ifPresent(bone -> {
                final Vector3d localPos = bone.getLocalPosition();
                Direction gravity = GravityChangerAPI.getGravityDirection(animatable);
                if (gravity.getAxis().isHorizontal()) {
                    gravity = gravity.getOpposite();
                }

                final Vec3 worldPos = RotationUtil.vecWorldToPlayer(localPos.x, localPos.y, localPos.z, gravity).add(animatable.position());

                animatable.getCommandSenderWorld().addParticle(ParticleTypes.FLAME,
                        worldPos.x, worldPos.y, worldPos.z,
                        0.0, 0.0, 0.0
                );
            });
        }
    }*/
}
