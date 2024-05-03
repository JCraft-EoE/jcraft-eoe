package net.arna.jcraft.client.renderer.entity.projectiles;

import net.arna.jcraft.client.model.entity.EmeraldModel;
import net.arna.jcraft.client.model.entity.KnifeModel;
import net.arna.jcraft.common.entity.projectile.EmeraldProjectile;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class EmeraldRenderer extends GeoEntityRenderer<EmeraldProjectile> {
    public EmeraldRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new EmeraldModel());
    }
}
