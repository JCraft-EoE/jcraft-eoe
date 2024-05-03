package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.TheWorldOverHeavenModel;
import net.arna.jcraft.client.renderer.entity.layer.TWOHEyesLayer;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class TheWorldOverHeavenRenderer extends StandEntityRenderer<TheWorldOverHeavenEntity> {

    public TheWorldOverHeavenRenderer(EntityRendererFactory.Context context) {
        super(context, new TheWorldOverHeavenModel());
        this.addRenderLayer(new TWOHEyesLayer(this));
    }
}
