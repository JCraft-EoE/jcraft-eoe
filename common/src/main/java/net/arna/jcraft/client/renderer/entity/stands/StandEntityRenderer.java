package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.client.renderer.entity.StandEntityModelRenderer;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * The {@link AbstractEntityRenderer} for stands of any {@link StandType StandType}.
 * @param <T> the entity to render
 */
@Environment(EnvType.CLIENT)
public class StandEntityRenderer<T extends StandEntity<?, ?>> extends AbstractEntityRenderer<T> {
    protected static final String TEXTURE_STR_TEMPLATE = "textures/entity/stands/%s/%s.png";

    protected static final Map<TypeSkin, ResourceLocation> TEXTURE_MAP = new HashMap<>();

    protected static <T extends StandEntity<?, ?>> StandEntityRenderer<T> of(
            final @NonNull AzEntityRendererConfig<T> config, final @NonNull EntityRendererProvider.Context context) {
        return new StandEntityRenderer<>(config, context);
    }
    protected StandEntityRenderer(final @NonNull AzEntityRendererConfig<T> config, final @NonNull EntityRendererProvider.Context context) {
        super(config, context);
    }

    protected StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs,
                                  final @NonNull Function<T, ResourceLocation> model, final @NonNull Function<T, ResourceLocation> texture,
                                  final @NonNull StandType type, final boolean flipBody, final boolean flipHead, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        super(additionalConfigs.apply(
                AzEntityRendererConfig.builder(model, texture)
                        .setAnimatorProvider(() -> new StandAnimator<>(type.getId().withPath(ANIMATION_STR_TEMPLATE::formatted),
                                flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence))
                        .setModelRenderer(StandEntityModelRenderer::new)
                        .setRenderType(renderType())
                        .setPrerenderEntry(preRenderEntry())
                        // .setRenderEntry(renderEntry())
        ).build(), context);
    }

    protected StandEntityRenderer(final @NonNull EntityRendererProvider.Context context,
                                  final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs,
                                  final @NonNull StandType type, final boolean flipBody, final boolean flipHead,
                                  final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        this(context, additionalConfigs,
                entity -> type.getId().withPath(MODEL_STR_TEMPLATE::formatted),
                StandEntityRenderer::getTextureLocation,
                type, flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence);
    }

    protected StandEntityRenderer(final @NonNull EntityRendererProvider.Context context,
                                  final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs,
                                  final @NonNull StandType type, final float torsoPitchOffset, final float headPitchOffset,
                                  final float velInfluence) {
        this(context, additionalConfigs, type, false, false, torsoPitchOffset, headPitchOffset, velInfluence);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context,
                               final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs,
                               final @NonNull StandType type, final float torsoPitchOffset, final float headPitchOffset) {
        this(context, additionalConfigs, type, false, false, torsoPitchOffset, headPitchOffset, 90f);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull StandType type,
                               final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        this(context, UnaryOperator.identity(), type, false, false, torsoPitchOffset, headPitchOffset, velInfluence);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull StandType type,
                               final float torsoPitchOffset, final float headPitchOffset) {
        this(context, UnaryOperator.identity(), type, false, false, torsoPitchOffset, headPitchOffset, 90f);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull StandType type) {
        this(context, UnaryOperator.identity(), type, 0f, 0f);
    }

    @Override
    public boolean shouldRender(@NotNull T livingEntity, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return JClientUtils.shouldRenderStands() && super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }

    protected static @NonNull <T extends StandEntity<?,?>> Function<T, RenderType> renderType() {
        return stand -> StandEntityRenderer.renderTypeOf(stand, getTextureLocation(stand));
    }

    protected static @NonNull <T extends StandEntity<?,?>> Function<T, RenderType> renderType(
            final @NonNull Function<ResourceLocation, RenderType> renderTypeSelector) {
        return stand -> renderTypeSelector.apply(getTextureLocation(stand));
    }

    public record TypeSkin(StandType type, Integer skin) {
        // intentionally left empty
    }

    protected static ResourceLocation typeSkinToTexture(final @NonNull TypeSkin ts) {
        return ts.type().getId().withPath(p -> TEXTURE_STR_TEMPLATE.formatted(p, ts.skin() == 0 ? "default" : "skin" + ts.skin()));
    }
    
    protected static @NonNull ResourceLocation getTextureLocation(final @NonNull StandEntity<?,?> stand) {
        return TEXTURE_MAP.computeIfAbsent(new TypeSkin(stand.getStandType(), stand.getSkin()), StandEntityRenderer::typeSkinToTexture);
    }

    protected static @NonNull ResourceLocation getTextureLocation(final @NonNull StandType type) {
        return TEXTURE_MAP.computeIfAbsent(new TypeSkin(type, 0), StandEntityRenderer::typeSkinToTexture);
    }

    protected static @NonNull <T extends StandEntity<?,?>> Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> preRenderEntry() {
        return pc -> {
            final var animatable = pc.animatable();
            final var partialTick = pc.partialTick();

            if (animatable.tickCount == 0) {
                pc.setAlpha(0.2f * partialTick);
                pc.poseStack().scale(partialTick, 1, partialTick);
                return pc;
            } else if (animatable.tickCount == 1) {
                if (animatable.getMoveStun() <= 0 && animatable.isPlaySummonAnim()) {
                    animatable.playSummonAnimation();
                } else {
                    // TODO: fix this hack. animations cant be played for entities that just spawned.
                    // this is also probably what stops the summon from working as intended.
                    animatable.playStateAnimation();
                }
            } else if (animatable.tickCount > animatable.getStandData().getSummonData().getAnimDuration()) { // average summon anim duration
                if (animatable.isIdle()) {
                    animatable.playStateAnimation();
                }
            }

            float a = getAlpha(animatable, partialTick);
            a *= pc.alpha();

            if (a > 0.01f) {
                pc.setAlpha(a);
            }

            return pc;
        };
    }

    /*
    private static @NonNull <T extends StandEntity<?,?>> Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> renderEntry() {
        return pc -> {
            final var animatable = pc.animatable();

            if (animatable.isPlaySummonAnim() && animatable.getMoveStun() <= 0) {
                StandEntity.SUMMON_ANIMATION.sendForEntity(animatable);
            }

            return pc;
        };
    }
     */

    public static boolean standIsFirstPersonViewers(final StandEntity<?, ?> stand) {
        final Minecraft mcClient = Minecraft.getInstance();
        return mcClient.options.getCameraType().isFirstPerson() && mcClient.player != null && JUtils.getStand(mcClient.player) == stand;
    }

    /*
    Cutout - no alpha
    CutoutNoCull - identical (hopium)
    Alpha - no lighting
    Translucent - with alpha, nothing renders through
    Decal - invisible
    NoOutline - transparent, everything is visible through
    Shadow - inverted normals, no alpha
    SmoothCutout - Cutout
    Solid - no transparency
     */
    public static RenderType renderTypeOf(final StandEntity<?, ?> stand, final ResourceLocation textureLocation) {
        return standIsFirstPersonViewers(stand) ? RenderType.entityNoOutline(textureLocation) : RenderType.entityTranslucent(textureLocation);
    }

    public static boolean shouldApplyAlpha(final StandEntity<?, ?> stand) {
        final Minecraft mcClient = Minecraft.getInstance();
        return mcClient.player != null && mcClient.options.getCameraType().isFirstPerson() && JUtils.getStand(mcClient.player) == stand;
    }

    public static float getAlpha(final StandEntity<?, ?> stand, final float tickDelta) {
        if (!shouldApplyAlpha(stand)) {
            return 1f;
        }

        // If we have an alpha override this tick and had one last tick too, just use that.
        if (stand.hasAlphaOverride() && stand.getPrevAlpha() >= 0) {
            return stand.getAlphaOverride();
        }

        final JClientConfig config = JClientConfig.getInstance();
        final float alphaMult = config.getFirstPersonStandOpacityMult();

        final float a =
                config.isDynamicFirstPersonStandOpacity() ?
                        alphaMult * Mth.clamp((float) stand.distanceToSqr(Minecraft.getInstance().player) / 2f, 0, 1) :
                        alphaMult;

        if (!stand.hasAlphaOverride()) {
            return a; // If we don't have an override, use this alpha value.
        }

        // If we do have an override, but didn't last tick, lerp between the previous alpha and the override.
        return Mth.lerp(tickDelta, a, stand.getAlphaOverride());
    }

    protected float getRed(final T stand, final float red, final float alpha) {
        return red;
    }

    protected float getGreen(final T stand, final float green, final float alpha) {
        return green;
    }

    protected float getBlue(final T stand, final float blue, final float alpha) {
        return blue;
    }

    public static class StandAnimator<T extends StandEntity<?,?>> extends EntityAnimator<T> {

        protected boolean flipBody;
        protected boolean flipHead;
        protected float torsoPitchOffset;
        protected float headPitchOffset;
        protected float velInfluence;

        public StandAnimator(final @NonNull ResourceLocation animation, final boolean flipBody, final boolean flipHead, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
            super(animation);
            this.flipBody = flipBody;
            this.flipHead = flipHead;
            this.torsoPitchOffset = torsoPitchOffset;
            this.headPitchOffset = headPitchOffset;
            this.velInfluence = velInfluence;
        }

        public StandAnimator(final @NonNull String id, final boolean flipBody, final boolean flipHead, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
            this(JCraft.id(ANIMATION_STR_TEMPLATE.formatted(id)), flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence);
        }

        @Override
        public void setCustomAnimations(final @NonNull T animatable, final float partialTicks) {
            JClientUtils.animateGenericHumanoid(context(), animatable, flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence);
        }
    }

    /**
     * The opacity that a held item should be rendered with so it matches the stand's first-person transparency.
     * Returns {@code 1f} (fully opaque) whenever the first-person fade does not apply.
     */
    public static float getItemRenderAlpha(final StandEntity<?, ?> stand, final float partialTick) {
        return shouldApplyAlpha(stand) ? getAlpha(stand, partialTick) : 1f;
    }

    public static class StandHandItemsRenderLayer<T extends StandEntity<?,?>> extends HandItemsRenderLayer<T> {
        @Override
        protected void renderItemForBone(final AzRendererPipelineContext<UUID, T> context, final AzBone bone, final ItemStack stack, final T animatable) {
            final var poseStack = context.poseStack();

            poseStack.mulPose(Axis.XP.rotationDegrees(bone.getRotX() * 57.29578f));
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));

            super.renderItemForBone(context, bone, stack, animatable);
        }

        @Override
        protected float getItemAlpha(final AzRendererPipelineContext<UUID, T> context, final T animatable) {
            return getItemRenderAlpha(animatable, context.partialTick());
        }
    }
}
