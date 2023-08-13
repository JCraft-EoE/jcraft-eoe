package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class SilverChariotModel extends StandEntityModel<SilverChariotEntity> {
    private static final Identifier NO_ARMOR_TEXTURE = JCraft.id("textures/entity/stands/silver_chariot/no_armor.png");
    private static final Identifier POSSESSED_TEXTURE = JCraft.id("textures/entity/stands/silver_chariot/possessed.png");

    public SilverChariotModel() {
        super(StandType.SILVER_CHARIOT);
    }

    @Override
    public Identifier getTextureResource(SilverChariotEntity entity) {
        return switch (entity.getMode()) {
            case 2 -> NO_ARMOR_TEXTURE;
            case 3 -> POSSESSED_TEXTURE;
            default -> super.getTextureResource(entity);
        };
    }

    @Override
    public void setCustomAnimations(SilverChariotEntity animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        if (animatable.hasUser())
            JClientUtils.animateGenericHumanoid(this, animatable, animatable.getUser(), animationEvent.getPartialTick(), true, true, 0, -0.2f);
    }
}
