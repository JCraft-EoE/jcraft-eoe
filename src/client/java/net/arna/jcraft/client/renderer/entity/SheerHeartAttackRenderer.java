package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.SheerHeartAttackModel;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SheerHeartAttackRenderer extends GeoEntityRenderer<SheerHeartAttackEntity> {

    public SheerHeartAttackRenderer(EntityRendererFactory.Context context) {
        super(context, new SheerHeartAttackModel());
    }

    @Override
    public RenderLayer getRenderType(SheerHeartAttackEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        return RenderLayer.getEntityTranslucent(this.getTextureLocation(animatable));
    }
}