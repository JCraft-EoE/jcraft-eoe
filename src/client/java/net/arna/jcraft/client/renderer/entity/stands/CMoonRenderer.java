package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3d;
import org.joml.Vector3f;
import software.bernie.geckolib.model.GeoModel;

public class CMoonRenderer extends StandEntityRenderer<CMoonEntity> {
    private int currentTick = -1;
    private static final int gravWindup = CMoonEntity.GRAV_PUNCH.getWindupPoint();
    private static final ParticleEffect chargeParticle = new DustParticleEffect(new Vector3f(0.8f, 0.2f, 1.0f), 2.0f);
    public CMoonRenderer(EntityRendererFactory.Context context) {
        super(context, new StandEntityModel<>(StandType.C_MOON));
    }

    @Override
    public void render(GeoModel model, CMoonEntity stand, float tickDelta, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexConsumer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.render(model, stand, tickDelta, type, matrixStackIn, vertexConsumerProvider, vertexConsumer, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (stand.getState() == CMoonEntity.State.GRAV_PUNCH && stand.getMoveStun() > gravWindup) {
            if (currentTick < 0 || currentTick != stand.age) {
                this.currentTick = stand.age;
                model.getBone("rightLower").ifPresent(bone -> {
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
