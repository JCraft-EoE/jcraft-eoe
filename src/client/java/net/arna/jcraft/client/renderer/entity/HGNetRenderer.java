package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.HGNetModel;
import net.arna.jcraft.client.renderer.entity.layer.HGNetGlowLayer;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HGNetRenderer extends GeoEntityRenderer<HGNetEntity> {
    public HGNetRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new HGNetModel());
        addRenderLayer(new HGNetGlowLayer(this));
        shadowRadius = 1.25f;
    }
}
