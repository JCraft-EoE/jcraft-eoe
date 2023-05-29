package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GETreeModel;
import net.arna.jcraft.common.entity.GETreeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class GETreeRenderer extends GeoProjectilesRenderer<GETreeEntity> {
    public GETreeRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new GETreeModel());
        this.shadowRadius = 3f;
    }
}
