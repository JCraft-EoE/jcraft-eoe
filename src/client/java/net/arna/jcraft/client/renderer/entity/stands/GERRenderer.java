package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class GERRenderer extends StandEntityRenderer<GEREntity> {

    public GERRenderer(EntityRendererFactory.Context context) {
        super(context, new StandEntityModel<>(StandType.GOLD_EXPERIENCE_REQUIEM));
    }
}
