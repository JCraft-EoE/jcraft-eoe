package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.PurpleHazeModel;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class PurpleHazeRenderer extends StandEntityRenderer<AbstractPurpleHazeEntity<?, ?>> {

    public PurpleHazeRenderer(EntityRendererFactory.Context context) {
        super(context, new PurpleHazeModel(false));
    }
}
