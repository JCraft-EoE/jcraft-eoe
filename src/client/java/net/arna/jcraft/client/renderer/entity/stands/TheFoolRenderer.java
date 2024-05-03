package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.TheFoolModel;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class TheFoolRenderer extends StandEntityRenderer<TheFoolEntity> {
    public TheFoolRenderer(EntityRendererFactory.Context context) {
        super(context, new TheFoolModel());
    }
}
