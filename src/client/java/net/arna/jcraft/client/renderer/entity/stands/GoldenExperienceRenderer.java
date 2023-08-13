package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.GoldenExperienceModel;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class GoldenExperienceRenderer extends StandEntityRenderer<GoldExperienceEntity> {

    public GoldenExperienceRenderer(EntityRendererFactory.Context context) {
        super(context, new GoldenExperienceModel());
    }
}
