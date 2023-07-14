package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StarPlatinumEntity;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class StarPlatinumModel extends AnimatedTickingGeoModel<StarPlatinumEntity> {
    //EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
    private final Identifier texture;
    private final Identifier animation;

    public StarPlatinumModel(String texture, String animation) {
        this.texture = JCraft.id(texture);
        this.animation = JCraft.id(animation);
    }

    @Override
    public Identifier getModelResource(StarPlatinumEntity object) {
        return JCraft.id("geo/starplatinum.geo.json");
    }

    @Override
    public Identifier getTextureResource(StarPlatinumEntity object) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(StarPlatinumEntity animatable) {
        return animation;
    }

    @Override
    public void setCustomAnimations(StarPlatinumEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser()) {
            JClientUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationEvent.getPartialTick(), true, true);

            if (animatable.getInhaleTime() > 0) {
                IBone head = getAnimationProcessor().getBone("head");

                World world = animatable.getWorld();
                for (int i = 0; i < 3; i++) {
                    /*
                    world.addParticle(
                            ParticleTypes.POOF,
                            fPos.x + random.nextDouble() - 0.5, fPos.y + random.nextDouble() - 0.5, fPos.z + random.nextDouble() - 0.5,
                            -rotVec.x / 3.0, -rotVec.y / 3.0, -rotVec.z / 3.0
                    );
                     */
                }
            }
        }
    }
}