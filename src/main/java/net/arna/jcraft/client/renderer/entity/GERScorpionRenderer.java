package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GERScorpionModel;
import net.arna.jcraft.entity.GERScorpionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class GERScorpionRenderer extends GeoProjectilesRenderer<GERScorpionEntity> {
    public GERScorpionRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new GERScorpionModel());
    }
}
