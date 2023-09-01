package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.common.entity.stand.StandEntity;
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
        return mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null && JUtils.getStand(mcClient.player) == stand ?
                RenderLayer.getEntityNoOutline(textureLocation) : RenderLayer.getEntityTranslucent(textureLocation);

    }

    // Adds the ability to change render alpha
    @Override
    public void render(GeoModel model, T stand, float tickDelta, RenderLayer type, MatrixStack matrixStackIn,
                       VertexConsumerProvider vertexConsumerProvider, VertexConsumer vertexConsumer, int packedLightIn,
                       int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = getAlpha(stand, tickDelta);
        a *= alpha;
        if (a <= 0.01f) return;

        super.render(model, stand, tickDelta, type, matrixStackIn, vertexConsumerProvider, vertexConsumer, packedLightIn,
                packedOverlayIn, getRed(stand, red, a), getGreen(stand, green, a), getBlue(stand, blue, a), a);
    }

    public static boolean shouldApplyAlpha(StandEntity<?, ?> stand) {
        MinecraftClient mcClient = MinecraftClient.getInstance();
        return mcClient.player != null && mcClient.options.getPerspective().isFirstPerson() && JUtils.getStand(mcClient.player) == stand;
    }

    public static float getAlpha(StandEntity<?, ?> stand, float tickDelta) {
        if (!shouldApplyAlpha(stand)) return 1f;

        // If we have an alpha override this tick and had one last tick too, just use that.
        if (stand.hasAlphaOverride() && stand.getPrevAlpha() >= 0) return stand.getAlphaOverride();

        float a = MathHelper.clamp((float) stand.squaredDistanceTo(MinecraftClient.getInstance().player) / 2f, 0, 1);
        if (!stand.hasAlphaOverride()) return a; // If we don't have an override, use this alpha value.

        // If we do have an override, but didn't last tick, lerp between the previous alpha and the override.
        return MathHelper.lerp(tickDelta, a, stand.getAlphaOverride());
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
