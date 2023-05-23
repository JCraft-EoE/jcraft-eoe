package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.TheFoolEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class TheFoolModel extends AnimatedTickingGeoModel<TheFoolEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(TheFoolEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/thefool.geo.json");
    }

    @Override
    public Identifier getTextureResource(TheFoolEntity object) {
        return new Identifier(JCraft.MOD_ID, object.isSand() ? "textures/entity/thefoolsand.png" : "textures/entity/thefool.png");
    }

    @Override
    public Identifier getAnimationResource(TheFoolEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/thefool.animation.json");
    }

    @Override
    public void setCustomAnimations(TheFoolEntity entity, int uniqueID, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, uniqueID, animationEvent);

        if (entity.hasUser()) {
            LivingEntity user = entity.getUser();
            float overVel = 0;
            float velInfluence = 30f;

            if (entity.getMoveStun() < 1) {
                IBone torso = this.getAnimationProcessor().getBone("torso");

                Vec3d userVel = user.getVelocity();
                overVel = (float)userVel.horizontalLength() - 0.05f;
                if (userVel.normalize().add(entity.getRotationVector()).horizontalLengthSquared() < userVel.normalize().horizontalLengthSquared()) { velInfluence *= -1; }
                if (torso != null) {
                    torso.setRotationX( (-overVel * velInfluence) * 3.1415f / 180f );
                }
            }

            IBone head = this.getAnimationProcessor().getBone("head");

            if (head != null) {
                head.setRotationX( (-user.getPitch() + overVel * velInfluence) * 3.1415f / 180f );
            }
        }
    }
}