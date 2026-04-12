package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/**
 * The {@link StandEntityRenderer} for {@link GoldExperienceEntity}.
 */
@Environment(EnvType.CLIENT)
public class GoldExperienceRenderer extends StandEntityRenderer<GoldExperienceEntity> {
    private int currentTick = -1;
    private static final ParticleOptions chargeParticle = ParticleTypes.COMPOSTER;

    public GoldExperienceRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.GOLD_EXPERIENCE.get(), 0, -0.1f);
    }

    /*
    @Override
    public void actuallyRender(final PoseStack poseStack, final GoldExperienceEntity stand, final BakedGeoModel model,
                               final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer,
                               final boolean isReRender, final float partialTick, final int packedLight, final int packedOverlay,
                               final float red, final float green, final float blue, final float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, getAlpha(animatable, partialTick));

        if (stand.getState() == GoldExperienceEntity.State.OVERCLOCK && stand.getCurrentMove() != null &&
                stand.getMoveStun() > stand.getCurrentMove().getWindupPoint()) {
            if (currentTick < 0 || currentTick != stand.tickCount) {
                this.currentTick = stand.tickCount;
                model.getBone("lowerleft").ifPresent(bone -> {
                    final RandomSource random = stand.getRandom();
                    final Vector3d worldPos = bone.getWorldPosition();
                    final Vec3 standVel = JUtils.deltaPos(stand);

                    stand.getCommandSenderWorld().addParticle(chargeParticle,
                            worldPos.x, worldPos.y, worldPos.z,
                            standVel.x + random.nextGaussian() * 0.3,
                            standVel.y + random.nextGaussian() * 0.3,
                            standVel.z + random.nextGaussian() * 0.3
                    );
                });
            }
        }
    }*/
}
