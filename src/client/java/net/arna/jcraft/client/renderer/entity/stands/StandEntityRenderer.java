package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.common.entity.StandEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
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
                RenderLayer.getEntityNoOutline(getTextureLocation(stand)) : RenderLayer.getEntityTranslucent(getTextureLocation(stand));

    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, T stand, float partialTicks, RenderLayer type, MatrixStack matrixStackIn,
                       VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexConsumer, int packedLightIn,
                       int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = getAlpha(stand);
        a *= alpha;
        if (a <= 0.01f) return;

        super.render(model, stand, partialTicks, type, matrixStackIn, vertexConsumerProvider, vertexConsumer, packedLightIn,
                packedOverlayIn, getRed(stand, red, a), getGreen(stand, green, a), getBlue(stand, blue, a), a);
    }

    protected float getInitialAlpha(T stand) {
        return 1f;
    }

    protected float getAlpha(T stand) {
        float a = getInitialAlpha(stand);

        // When in first person, draws translucently
        // When in third person, solid
        // When viewed by others, solid

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.player != null && mcClient.options.getPerspective().isFirstPerson() && mcClient.player.getFirstPassenger() == stand)
            a = stand.getAlpha();

        return a;
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
