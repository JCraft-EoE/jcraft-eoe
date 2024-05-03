package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.GETreeModel;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GETreeRenderer extends GeoEntityRenderer<GETreeEntity> {
    public GETreeRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new GETreeModel());
        shadowRadius = 2.5f;
    }
}
