package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.GoldenExperienceModel;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class GoldExperienceRenderer extends StandEntityRenderer<GoldExperienceEntity> {
    private int currentTick = -1;
    private static final int overclockWindupPoint = GoldExperienceEntity.OVERCLOCK.getWindupPoint();
    private static final ParticleEffect chargeParticle = ParticleTypes.COMPOSTER;
    public GoldExperienceRenderer(EntityRendererFactory.Context context) {
        super(context, new GoldenExperienceModel());
    }

    @Override
    public void render(GeoModel model, GoldExperienceEntity stand, float tickDelta, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexConsumer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.render(model, stand, tickDelta, type, matrixStackIn, vertexConsumerProvider, vertexConsumer, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (stand.getState() == GoldExperienceEntity.State.OVERCLOCK && stand.getMoveStun() > overclockWindupPoint) {
            if (currentTick < 0 || currentTick != stand.age) {
                this.currentTick = stand.age;
                model.getBone("lowerleft").ifPresent(bone -> {
                    Random random = stand.getRandom();
                    Vector3d worldPos = bone.getWorldPosition();
                    Vec3d standVel = JUtils.deltaPos(stand);

                    stand.getEntityWorld().addParticle(chargeParticle,
                            worldPos.x, worldPos.y, worldPos.z,
                            standVel.x + random.nextGaussian() * 0.3,
                            standVel.y + random.nextGaussian() * 0.3,
                            standVel.z + random.nextGaussian() * 0.3
                    );
                });
            }
        }
    }
}
