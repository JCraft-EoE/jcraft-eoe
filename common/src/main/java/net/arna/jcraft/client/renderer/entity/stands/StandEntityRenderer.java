package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.client.renderer.entity.StandEntityModelRenderer;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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
    protected static final ResourceLocation SAND_TEXTURE = JCraft.id("textures/entity/stands/the_fool/sand.png");
    protected static final ResourceLocation ARMORLESS_TEXTURE = JCraft.id("textures/entity/stands/silver_chariot/no_armor.png");
    protected static final ResourceLocation[] POSSESSED_TEXTURES = new ResourceLocation[] {
            JCraft.id("textures/entity/stands/silver_chariot/possessed0.png"),
            JCraft.id("textures/entity/stands/silver_chariot/possessed1.png"),
            JCraft.id("textures/entity/stands/silver_chariot/possessed2.png"),
            JCraft.id("textures/entity/stands/silver_chariot/possessed3.png")
    };

    protected static final Map<TypeSkin, ResourceLocation> TEXTURE_MAP = new HashMap<>();

    protected static <T extends StandEntity<?, ?>> StandEntityRenderer<T> of(
            final @NonNull AzEntityRendererConfig<T> config, final @NonNull EntityRendererProvider.Context context,
            final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        return new StandEntityRenderer<>(config, context, model, texture);
    }
    protected StandEntityRenderer(final @NonNull AzEntityRendererConfig<T> config, final @NonNull EntityRendererProvider.Context context,
                                final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        super(config, context, model, texture);
    }

    protected StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs,
                                  final @NonNull Function<T, ResourceLocation> model, final @NonNull Function<T, ResourceLocation> texture,
                                  final @NonNull StandType type, final boolean flipBody, final boolean flipHead, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        super(additionalConfigs.apply(
                AzEntityRendererConfig.builder(model, texture)
                        .setAnimatorProvider(() -> new StandAnimator<>(type.getId().getPath(), flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence))
                        .setModelRenderer(StandEntityModelRenderer::new)
                        .setRenderType(renderType())
                        .setPrerenderEntry(preRenderEntry())
                        // .setRenderEntry(renderEntry())
        ).build(), context, type.getId().getPath());
    }

    protected StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs, final @NonNull StandType type, final boolean flipBody, final boolean flipHead, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        this(context, additionalConfigs,
                entity -> type.getId().withPath(MODEL_STR_TEMPLATE.formatted(type.getId().getPath())),
                StandEntityRenderer::getTextureLocation,
                type, flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence);
    }

    protected StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs, final @NonNull StandType type, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        this(context, additionalConfigs, type, false, false, torsoPitchOffset, headPitchOffset, velInfluence);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> additionalConfigs, final @NonNull StandType type, final float torsoPitchOffset, final float headPitchOffset) {
        this(context, additionalConfigs, type, false, false, torsoPitchOffset, headPitchOffset, 90f);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull StandType type, final float torsoPitchOffset, final float headPitchOffset, final float velInfluence) {
        this(context, UnaryOperator.identity(), type, false, false, torsoPitchOffset, headPitchOffset, velInfluence);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull StandType type, final float torsoPitchOffset, final float headPitchOffset) {
        this(context, UnaryOperator.identity(), type, false, false, torsoPitchOffset, headPitchOffset, 90f);
    }

    public StandEntityRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull StandType type) {
        this(context, UnaryOperator.identity(), type, 0f, 0f);
    }

    protected static @NonNull <T extends StandEntity<?,?>> Function<T, RenderType> renderType() {
        return stand -> StandEntityRenderer.renderTypeOf(stand, getTextureLocation(stand));
    }

    protected static @NonNull <T extends StandEntity<?,?>> Function<T, RenderType> renderType(final @NonNull Function<ResourceLocation, RenderType> renderTypeSelector) {
        return stand -> renderTypeSelector.apply(getTextureLocation(stand));
    }

    public record TypeSkin(StandType type, Integer skin) {
        // intentionally left empty
    }

    protected static ResourceLocation typeSkinToTexture(final @NonNull TypeSkin ts) {
        return ts.type().getId().withPath(TEXTURE_STR_TEMPLATE.formatted(ts.type().getId().getPath(), ts.skin() == 0 ? "default" : "skin" + ts.skin()));
    }
    
    protected static @NonNull ResourceLocation getTextureLocation(final @NonNull StandEntity<?,?> stand) {
        final int skin = stand.getSkin();
        final StandType type = stand.getStandType();

        if (stand instanceof TheFoolEntity fool && fool.isSand()) {
            return SAND_TEXTURE;
        } else if (stand instanceof SilverChariotEntity chariot) {
            final SilverChariotEntity.Mode mode = chariot.getMode();

            if (mode == SilverChariotEntity.Mode.ARMORLESS) {
                return ARMORLESS_TEXTURE;
            } else if (mode == SilverChariotEntity.Mode.POSSESSED) {
                return POSSESSED_TEXTURES[chariot.getSkin()];
            }
        }

        return TEXTURE_MAP.computeIfAbsent(new TypeSkin(type, skin), StandEntityRenderer::typeSkinToTexture);
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
}
