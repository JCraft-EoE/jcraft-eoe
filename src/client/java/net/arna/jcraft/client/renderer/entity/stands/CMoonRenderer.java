package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class CMoonRenderer extends StandEntityRenderer<CMoonEntity> {

    public CMoonRenderer(EntityRendererFactory.Context context) {
        super(context, new StandEntityModel<>(StandType.C_MOON));
    }
}
