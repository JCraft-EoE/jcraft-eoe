package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.KQBTDModel;
import net.arna.jcraft.client.renderer.entity.layer.KQBTDEyesLayer;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class KQBTDRenderer extends StandEntityRenderer<KQBTDEntity> {

    public KQBTDRenderer(EntityRendererFactory.Context context) {
        super(context, new KQBTDModel());
        this.addLayer(new KQBTDEyesLayer(this));
    }
}
