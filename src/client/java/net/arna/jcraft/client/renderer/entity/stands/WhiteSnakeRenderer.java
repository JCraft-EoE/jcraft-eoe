package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.WhiteSnakeModel;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class WhiteSnakeRenderer extends StandEntityRenderer<WhiteSnakeEntity> {

    public WhiteSnakeRenderer(EntityRendererFactory.Context context) {
        super(context, new WhiteSnakeModel());
    }
}
