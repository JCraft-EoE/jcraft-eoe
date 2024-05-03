package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class KingCrimsonModel extends StandEntityModel<KingCrimsonEntity> {

    public KingCrimsonModel() {
        super(StandType.KING_CRIMSON);
    }



    @Override
    public void setCustomAnimations(KingCrimsonEntity animatable, long instanceId, AnimationState<KingCrimsonEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        if (!animatable.hasUser()) return;

        LivingEntity user = animatable.getUserOrThrow();
        float overVel = 0;
        float velInfluence = 90f;

        if (animatable.getMoveStun() < 1) {
            CoreGeoBone torso = this.getAnimationProcessor().getBone("torso");

            Vec3d userVel = user.getVelocity();
            overVel = (float) userVel.horizontalLength() - 0.05f;
            if (userVel.normalize().add(animatable.getRotationVector()).horizontalLengthSquared() < userVel.normalize().horizontalLengthSquared()) {
                velInfluence *= -1;
            }
            if (torso != null) {
                torso.setRotX((overVel * velInfluence) * 3.1415f / 180f);
            }
        }

        CoreGeoBone head = this.getAnimationProcessor().getBone("head");

        if ((animatable.isBlocking() || animatable.isIdle()) && head != null) {
            head.setRotX(-(user.getPitch() + overVel * velInfluence) * 3.1415f / 180f);

        } else if (animatable.getMoveStun() > 0) {
            CoreGeoBone torso = this.getAnimationProcessor().getBone("base");
            if (torso != null) {
                float torsoPitch = (user.getPitch() * 0.9f) * 3.1415f / 180f;
                torso.setRotX(torso.getRotX() - torsoPitch);
            }
        }
    }

    @Override
    protected boolean skipCustomAnimations() {
        return true;
    }
}
