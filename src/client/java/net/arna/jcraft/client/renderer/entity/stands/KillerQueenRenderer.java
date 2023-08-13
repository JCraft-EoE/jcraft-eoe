package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.KillerQueenModel;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class KillerQueenRenderer extends StandEntityRenderer<KillerQueenEntity> {

    public KillerQueenRenderer(EntityRendererFactory.Context context) {
        super(context, new KillerQueenModel());
    }
}
