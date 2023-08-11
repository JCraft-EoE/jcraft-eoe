package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class StandEntityRenderer<T extends StandEntity<?, ?>> extends GeoEntityRenderer<T> {

    protected StandEntityRenderer(EntityRendererFactory.Context renderManager, AnimatedGeoModel<T> modelProvider) {
        super(renderManager, modelProvider);
    }

    /*
    Cutout - no alpha
    CutoutNoCull - identical (copium)
    Alpha - no lighting
    Translucent - with alpha, nothing renders through
    Decal - invisible
    NoOutline - transparent, everything is visible through
    Shadow - inverted normals, no alpha
    SmoothCutout - Cutout
    Solid - no transparency
     */
    @Override
    public RenderLayer getRenderType(T stand, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        MinecraftClient mcClient = MinecraftClient.getInstance();
        return mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null && mcClient.player.getFirstPassenger() == stand ?
                RenderLayer.getEntityNoOutline(textureLocation) : RenderLayer.getEntityTranslucent(textureLocation);

    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, T stand, float tickDelta, RenderLayer type, MatrixStack matrixStackIn,
                       VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexConsumer, int packedLightIn,
                       int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = shouldApplyAlpha(stand) ? MathHelper.lerp(tickDelta, stand.getPrevAlpha(), stand.getAlpha()) : getInitialAlpha(stand);
        a *= alpha;
        if (a <= 0.01f) return;

        super.render(model, stand, tickDelta, type, matrixStackIn, vertexConsumerProvider, vertexConsumer, packedLightIn,
                packedOverlayIn, getRed(stand, red, a), getGreen(stand, green, a), getBlue(stand, blue, a), a);
    }

    protected float getInitialAlpha(T stand) {
        return 1f;
    }

    protected boolean shouldApplyAlpha(T stand) {
        MinecraftClient mcClient = MinecraftClient.getInstance();
        return mcClient.player != null && mcClient.options.getPerspective().isFirstPerson() && JUtils.getStand(mcClient.player) == stand;
    }

    protected float getRed(T stand, float red, float alpha) {
        return red;
    }

    protected float getGreen(T stand, float green, float alpha) {
        return green;
    }

    protected float getBlue(T stand, float blue, float alpha) {
        return blue;
    }
}
