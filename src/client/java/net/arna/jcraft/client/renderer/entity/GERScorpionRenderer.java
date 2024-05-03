package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GERScorpionModel;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GERScorpionRenderer extends GeoEntityRenderer<GERScorpionEntity> {
    public GERScorpionRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new GERScorpionModel());
    }
}
