package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.StarPlatinumModel;
import net.arna.jcraft.common.entity.stand.AbstractStarPlatinumEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class SPTWRenderer extends StandEntityRenderer<AbstractStarPlatinumEntity<?, ?>> {

    public SPTWRenderer(EntityRendererFactory.Context context) {
        super(context, new StarPlatinumModel(true));
    }
}
