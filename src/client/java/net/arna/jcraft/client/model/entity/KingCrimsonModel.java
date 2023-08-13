package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class KingCrimsonModel extends StandEntityModel<KingCrimsonEntity> {

    public KingCrimsonModel() {
        super(StandType.KING_CRIMSON);
    }

    @Override
    public void setCustomAnimations(KingCrimsonEntity entity, int uniqueID, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, uniqueID, animationEvent);
        if (!entity.hasUser()) return;

        LivingEntity user = entity.getUserOrThrow();
        float overVel = 0;
        float velInfluence = 90f;

        if (entity.getMoveStun() < 1) {
            IBone torso = this.getAnimationProcessor().getBone("torso");

            Vec3d userVel = user.getVelocity();
            overVel = (float) userVel.horizontalLength() - 0.05f;
            if (userVel.normalize().add(entity.getRotationVector()).horizontalLengthSquared() < userVel.normalize().horizontalLengthSquared()) {
                velInfluence *= -1;
            }
            if (torso != null) {
                torso.setRotationX((overVel * velInfluence) * 3.1415f / 180f);
            }
        }

        IBone head = this.getAnimationProcessor().getBone("head");

        if ((entity.isBlocking() || entity.isIdle()) && head != null) {
            head.setRotationX(-(user.getPitch() + overVel * velInfluence) * 3.1415f / 180f);
        }
    }

    @Override
    protected boolean skipCustomAnimations() {
        return true;
    }
}
