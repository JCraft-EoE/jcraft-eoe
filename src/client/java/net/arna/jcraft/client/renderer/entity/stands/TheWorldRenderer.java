package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.TheWorldModel;
import net.arna.jcraft.common.entity.stand.TheWorldEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class TheWorldRenderer extends StandEntityRenderer<TheWorldEntity> {

    public TheWorldRenderer(EntityRendererFactory.Context context) {
        super(context, new TheWorldModel());
    }
}
