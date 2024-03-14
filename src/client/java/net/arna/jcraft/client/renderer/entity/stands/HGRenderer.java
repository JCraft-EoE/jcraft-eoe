package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.HGModel;
import net.arna.jcraft.common.entity.stand.HGEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class HGRenderer extends StandEntityRenderer<HGEntity> {
    public HGRenderer(EntityRendererFactory.Context context) {
        super(context, new HGModel());
    }
}