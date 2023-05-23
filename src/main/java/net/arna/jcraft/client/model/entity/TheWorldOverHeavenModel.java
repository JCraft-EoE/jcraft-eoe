package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.TheWorldOverHeavenEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class TheWorldOverHeavenModel extends AnimatedTickingGeoModel<TheWorldOverHeavenEntity> {

    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

    @Override
    public Identifier getModelResource(TheWorldOverHeavenEntity object) {
        return new Identifier(JCraft.MOD_ID, "geo/twoh.geo.json");
    }

    @Override
    public Identifier getTextureResource(TheWorldOverHeavenEntity object) {
        return new Identifier(JCraft.MOD_ID, "textures/entity/twoh.png");
    }

    @Override
    public Identifier getAnimationResource(TheWorldOverHeavenEntity animatable) {
        return new Identifier(JCraft.MOD_ID, "animations/twoh.animation.json");
    }

    @Override
    public void setCustomAnimations(TheWorldOverHeavenEntity entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);

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
                    torso.setRotationX( (180f + overVel * velInfluence) * 3.1415f / 180f );
                }
            }

            IBone head = this.getAnimationProcessor().getBone("head");

            if (head != null) {
                float pOffset = 0f;
                if (entity.getState() == 1) { pOffset += 7.5f; }
                if (entity.getState() == 4) { pOffset -= 25f; }
                head.setRotationX( (user.getPitch() + pOffset - overVel * velInfluence) * 3.1415f / 180f);
            }
        }
    }
}