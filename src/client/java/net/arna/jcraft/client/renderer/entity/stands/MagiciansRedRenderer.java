package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.MagiciansRedModel;
import net.arna.jcraft.client.renderer.entity.layer.MRGlowLayer;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class MagiciansRedRenderer extends StandEntityRenderer<MagiciansRedEntity> {

    public MagiciansRedRenderer(EntityRendererFactory.Context context) {
        super(context, new MagiciansRedModel());
        addLayer(new MRGlowLayer(this));
    }
}
