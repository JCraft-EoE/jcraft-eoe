package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GETreeModel;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class GETreeRenderer extends GeoProjectilesRenderer<GETreeEntity> {
    public GETreeRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new GETreeModel());
        shadowRadius = 2.5f;
    }
}
