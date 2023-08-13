package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.KingCrimsonModel;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class KingCrimsonRenderer extends StandEntityRenderer<KingCrimsonEntity> {

    public KingCrimsonRenderer(EntityRendererFactory.Context context) {
        super(context, new KingCrimsonModel());
        //this.addLayer(new KCTELayer(this));
    }

    @Override
    protected float getGreen(KingCrimsonEntity stand, float green, float alpha) {
        return green - (1f - alpha);
    }

    @Override
    protected float getBlue(KingCrimsonEntity stand, float blue, float alpha) {
        return blue - (1f - alpha);
    }
}
