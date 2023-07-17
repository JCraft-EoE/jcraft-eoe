package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.entity.StandType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

import java.util.List;
import java.util.stream.IntStream;

public class StandEntityModel<E extends StandEntity> extends AnimatedTickingGeoModel<E> {
    private final StandType type;
    private final Identifier model;
    private final List<Identifier> skins;
    private final Identifier animation;
    private final float torsoPitchOffset, headPitchOffset, velInfluence;

    public StandEntityModel(StandType type) {
        this(type, 0f, 0f);
    }
    
    public StandEntityModel(StandType type, float torsoPitchOffset, float headPitchOffset) {
        this(type, torsoPitchOffset, headPitchOffset, 90f);
    }
    
    public StandEntityModel(StandType type, float torsoPitchOffset, float headPitchOffset, float velInfluence) {
        this.type = type;
        String typeName = type.name().toLowerCase();
        model = JCraft.id("geo/" + typeName + ".geo.json");
        skins = IntStream.rangeClosed(0, type.getSkinCount())
                .mapToObj(i -> JCraft.id("textures/entity/stands/" + typeName + "/" + (i == 0 ? "default" : "skin" + i) + ".png"))
                .toList();
        animation = JCraft.id("animations/" + typeName + ".animation.json");
        
        this.torsoPitchOffset = torsoPitchOffset;
        this.headPitchOffset = headPitchOffset;
        this.velInfluence = velInfluence;
    }
    
    @Override
    public Identifier getModelResource(E entity) {
        return model;
    }

    @Override
    public Identifier getTextureResource(E entity) {
        return skins.get(MathHelper.clamp(entity.getSkin(), 0, type.getSkinCount()));
    }

    @Override
    public Identifier getAnimationResource(E entity) {
        return animation;
    }

    @Override
    public void setCustomAnimations(E entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        if (skipCustomAnimations() || !entity.hasUser()) return;
        
        JClientUtils.animateGenericHumanoid(this, entity, entity.getUser(), animationEvent.getPartialTick(), 
                true, true, torsoPitchOffset, headPitchOffset, velInfluence);
    }
    
    protected boolean skipCustomAnimations() {
        return false;
    }
}
