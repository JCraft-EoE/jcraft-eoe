package net.arna.jcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.CarbonDioxideRadarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CarbonDioxideRadarRenderer extends AbstractEntityRenderer<CarbonDioxideRadarEntity> {
    private static final ResourceLocation MODEL = JCraft.id("geo/carbon_dioxide_radar.geo.json");
    private static final ResourceLocation ANIM = JCraft.id("animations/carbon_dioxide_radar.animation.json");

    private static final ResourceLocation TEX_DEF = JCraft.id("textures/entity/carbon_dioxide_radar/default.png");
    private static final ResourceLocation TEX_SKIN1 = JCraft.id("textures/entity/carbon_dioxide_radar/skin1.png");
    private static final ResourceLocation TEX_SKIN2 = JCraft.id("textures/entity/carbon_dioxide_radar/skin2.png");
    private static final ResourceLocation TEX_SKIN3 = JCraft.id("textures/entity/carbon_dioxide_radar/skin3.png");

    /** How far below the user's eye to sit. Negative numbers raise; positive lowers. */
    private static final double HEAD_Y_OFFSET = 0.25;

    /**
     * Approximate distance from the swimming/elytra-flying player's bbox center out to
     * where the head ends up in world space. The player model is rotated so the body
     * lies along the look vector; the head is on the far end roughly this far away.
     */
    private static final double SWIM_HEAD_FWD = 0.7;

    public CarbonDioxideRadarRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(
                AzEntityRendererConfig.builder(e -> MODEL, CarbonDioxideRadarRenderer::texture)
                        .setAnimatorProvider(RadarAnimator::new)
                        .setAlpha(CarbonDioxideRadarRenderer::alpha)
                        .build(),
                context
        );
    }

    private static ResourceLocation texture(final CarbonDioxideRadarEntity entity) {
        return switch (entity.getSkin()) {
            case 1  -> TEX_SKIN1;
            case 2  -> TEX_SKIN2;
            case 3  -> TEX_SKIN3;
            default -> TEX_DEF;
        };
    }

    /**
     * Alpha provider read by AzureLib's pipeline once per frame. Drops to 50% when
     * this radar belongs to the camera-bound entity and the camera is in first
     * person — otherwise the model fills a huge fraction of the view from inside
     * the player's own head. Any return value strictly less than 1 triggers the
     * alpha pass in {@code AzEntityRendererPipeline.preRender}, so we return
     * exactly {@code 1F} in the normal case to keep the fast path.
     */
    private static Float alpha(final CarbonDioxideRadarEntity entity) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson()
                && mc.getCameraEntity() == entity.getUser()) {
            return 0.5F;
        }
        return 1.0F;
    }

    /**
     * Overrides the render entry point so we can correct the entity's visual position
     * and orientation to the user's <em>sub-tick-interpolated</em> values.
     *
     * <p>{@code EntityRenderDispatcher} translates the pose stack by
     * {@code Mth.lerp(partialTick, entity.xo, entity.getX()) - cameraPos} before
     * this method runs, so editing {@code entity.setPos(...)} here is too late —
     * the visual position is already locked in. Instead, we add a corrective
     * {@code poseStack.translate(delta)} that turns the dispatcher's translation
     * into one that ends at the user's exact head position.
     *
     * <p>Rotation is set on the entity fields because AzureLib's
     * {@code applyRotations} reads {@code animatable.getYRot()} directly, and the
     * {@code setCustomAnimations} below reads {@code getXRot()}.
     */
    @Override
    public void render(final @NonNull CarbonDioxideRadarEntity entity, final float entityYaw, final float partialTick,
                       final @NonNull PoseStack poseStack, final @NonNull MultiBufferSource bufferSource,
                       final int packedLight) {
        final LivingEntity user = entity.getUser();
        if (user != null) {
            updateRotation(entity, partialTick, poseStack, user);
        }

        // Pass the freshly updated yRot as entityYaw so AzureLib's applyRotations uses
        // the interpolated head yaw rather than the stale value vanilla computed before
        // this override ran.
        super.render(entity, entity.getYRot(), partialTick, poseStack, bufferSource, packedLight);
    }

    private static void updateRotation(final @NotNull CarbonDioxideRadarEntity entity, final float partialTick,
                                       final @NotNull PoseStack poseStack, final LivingEntity user) {
        // Smoothly-lerped user foot position (sub-tick precision).
        final double userX = Mth.lerp(partialTick, user.xo, user.getX());
        final double userY = Mth.lerp(partialTick, user.yo, user.getY());
        final double userZ = Mth.lerp(partialTick, user.zo, user.getZ());

        // Where the user's visible head actually is in world space.
        final double headX, headY, headZ;
        final boolean horizontal = user.isVisuallySwimming() || user.isFallFlying();
        if (horizontal) {
            // The vanilla LivingEntityRenderer rotates the model around X so the
            // body lies along the look vector — head ends up forward of the bbox
            // centre rather than above the feet. We approximate that here by
            // stepping out along the player's view vector.
            final Vec3 look = user.getViewVector(partialTick);
            final double centerY = userY + user.getBbHeight() * 0.5;
            headX = userX + look.x * SWIM_HEAD_FWD;
            headY = centerY + look.y * SWIM_HEAD_FWD;
            headZ = userZ + look.z * SWIM_HEAD_FWD;
        } else {
            // Upright pose — sit just below the user's eye level so the radar
            // body lines up with the top of the head rather than floating above it.
            headX = userX;
            headY = userY + user.getEyeHeight() - HEAD_Y_OFFSET;
            headZ = userZ;
        }

        // Reproduce what the dispatcher already used to translate the pose stack.
        final double entLerpX = Mth.lerp(partialTick, entity.xo, entity.getX());
        final double entLerpY = Mth.lerp(partialTick, entity.yo, entity.getY());
        final double entLerpZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        // Adding the delta lands the radar at exactly (headX, headY, headZ).
        poseStack.translate(headX - entLerpX, headY - entLerpY, headZ - entLerpZ);

        // Yaw is consumed by AzureLib's applyRotations(animatable.getYRot()).
        final float headYaw = Mth.lerp(partialTick, user.yHeadRotO, user.yHeadRot);
        entity.setYRot(headYaw);
        entity.yRotO = headYaw;

        // Pitch is consumed in setCustomAnimations below.
        final float headPitch = Mth.lerp(partialTick, user.xRotO, user.getXRot());
        entity.setXRot(headPitch);
        entity.xRotO = headPitch;
    }

    private static class RadarAnimator extends EntityAnimator<CarbonDioxideRadarEntity> {

        public RadarAnimator() {
            super(ANIM);
        }

        @Override
        public void setCustomAnimations(final @NonNull CarbonDioxideRadarEntity animatable,
                                        final float partialTicks) {
            final AzBakedModel model = context().boneCache().getBakedModel();
            final AzBone base = model.getBoneOrNull("base");
            if (base == null) return;

            // The entity is positioned at the user's eye level by render(), so no Y
            // bone translation is needed.
            base.setRotX(-animatable.getXRot() * Mth.DEG_TO_RAD);
        }
    }
}
