package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arna.jcraft.client.model.entity.stand.StandEntityModel;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * The {@link StandEntityRenderer} for {@link CMoonEntity}.
 */
@Environment(EnvType.CLIENT)
public class CMoonRenderer extends StandEntityRenderer<CMoonEntity> {
    private int currentTick = -1;
    private static final ParticleOptions chargeParticle = new DustParticleOptions(new Vector3f(0.8f, 0.2f, 1.0f), 2.0f);

    public CMoonRenderer(final EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.C_MOON.get());
    }

    /*@Override
    public void actuallyRender(final PoseStack poseStack, final CMoonEntity stand, final BakedGeoModel model,
                               final RenderType renderType, final MultiBufferSource bufferSource,
                               final VertexConsumer buffer, final boolean isReRender, final float partialTick,
                               final int packedLight, final int packedOverlay, final float red,
                               final float green, final float blue, final float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, getAlpha(animatable, partialTick));

        if (stand.getState() == CMoonEntity.State.GRAV_PUNCH && stand.getCurrentMove() != null &&
                stand.getMoveStun() > stand.getCurrentMove().getWindupPoint()) {
            if (currentTick < 0 || currentTick != stand.tickCount) {
                this.currentTick = stand.tickCount;
                model.getBone("rightLower").ifPresent(bone -> {
                    RandomSource random = stand.getRandom();
                    Vector3d localPos = bone.getLocalPosition();
                    Vec3 worldPos = RotationUtil.vecWorldToPlayer(localPos.x, localPos.y, localPos.z, GravityChangerAPI.getGravityDirection(animatable)).add(animatable.position());
                    Vec3 standVel = JUtils.deltaPos(stand);

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
