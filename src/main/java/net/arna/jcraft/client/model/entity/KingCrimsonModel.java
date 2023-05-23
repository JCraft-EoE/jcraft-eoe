package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.KingCrimsonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class KingCrimsonModel extends AnimatedTickingGeoModel<KingCrimsonEntity> {

    @Override
    public Identifier getModelResource(KingCrimsonEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/kingcrimson.geo.json");
    }

    @Override
    public Identifier getTextureResource(KingCrimsonEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/kingcrimson.png");
    }

    @Override
    public Identifier getAnimationResource(KingCrimsonEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/kingcrimson.animation.json");
    }

    @Override
    public void setCustomAnimations(KingCrimsonEntity entity, int uniqueID, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, uniqueID, animationEvent);

        if (entity.hasUser()) {
            LivingEntity user = entity.getUser();
            float overVel = 0;
            float velInfluence = 90f;

            if (entity.getMoveStun() < 1) {
                IBone torso = this.getAnimationProcessor().getBone("torso");

                Vec3d userVel = user.getVelocity();
                overVel = (float)userVel.horizontalLength() - 0.05f;
                if (userVel.normalize().add(entity.getRotationVector()).horizontalLengthSquared() < userVel.normalize().horizontalLengthSquared()) { velInfluence *= -1; }
                if (torso != null) {
                    torso.setRotationX( (overVel * velInfluence) * 3.1415f / 180f );
                }
            }

            IBone head = this.getAnimationProcessor().getBone("head");

            if ( (entity.getState() == 3 || entity.getState() < 2) && head != null) {
                head.setRotationX( -(user.getPitch() + overVel * velInfluence) * 3.1415f / 180f );
            }
        }
    }
}