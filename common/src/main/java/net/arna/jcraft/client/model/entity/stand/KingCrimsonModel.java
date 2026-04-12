package net.arna.jcraft.client.model.entity.stand;

import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The {@link StandEntityModel} for {@link KingCrimsonEntity}.
 * @see net.arna.jcraft.client.renderer.entity.stands.KingCrimsonRenderer KingCrimsonRenderer
 */
public class KingCrimsonModel extends StandEntityModel<KingCrimsonEntity> {

    public KingCrimsonModel() {
        super(JStandTypeRegistry.KING_CRIMSON.get());
    }

    /*@Override
    public void setCustomAnimations(final KingCrimsonEntity animatable, final long instanceId, final AnimationState<KingCrimsonEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (!animatable.hasUser()) {
            return;
        }

        final LivingEntity user = animatable.getUserOrThrow();
        float overVel = 0;
        float velInfluence = 90f;

        if (animatable.getMoveStun() < 1) {
            CoreGeoBone torso = this.getAnimationProcessor().getBone("torso");

            Vec3 userVel = user.getDeltaMovement();
            overVel = (float) userVel.horizontalDistance() - 0.05f;
            if (userVel.normalize().add(animatable.getLookAngle()).horizontalDistanceSqr() < userVel.normalize().horizontalDistanceSqr()) {
                velInfluence *= -1;
            }
            if (torso != null) {
                torso.setRotX((overVel * velInfluence) * 3.1415f / 180f);
            }
        }

        final CoreGeoBone head = this.getAnimationProcessor().getBone("head");

        if ((animatable.isBlocking() || animatable.isIdle()) && head != null) {
            head.setRotX(-(user.getXRot() + overVel * velInfluence) * 3.1415f / 180f);

        } else if (animatable.getMoveStun() > 0) {
            final CoreGeoBone torso = this.getAnimationProcessor().getBone("base");
            if (torso != null) {
                float torsoPitch = (user.getXRot() * 0.9f) * 3.1415f / 180f;
                torso.setRotX(torso.getRotX() - torsoPitch);
            }
        }
    }

    @Override
    protected boolean skipCustomAnimations() {
        return true;
    }*/
}
