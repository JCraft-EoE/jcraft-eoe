package net.arna.jcraft.client.renderer.armor;

import lombok.NonNull;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererConfig;
import mod.azure.azurelib.render.armor.AzArmorRendererPipelineContext;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.mixin.client.PlayerModelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ArmorRenderer extends AzArmorRenderer {

    protected static final String MODEL_STR_TEMPLATE = "geo/%s.geo.json";
    protected static final String TEXTURE_STR_TEMPLATE = "textures/armor/%s.png";
    protected static final String ANIMATION_STR_TEMPLATE = "animations/%s.animation.json";

    protected static final ArmorBoneProvider BONE_PROVIDER = new ArmorBoneProvider();

    /**
     * Path to the model to be used for this armor.
     */
    protected final @NonNull ResourceLocation model;
    /**
     * Path to the texture to be used for this armor.
     */
    protected final @NonNull ResourceLocation texture;

    /**
     * Constructs a renderer with a fully customizable config and the given model/texture paths.
     */
    protected ArmorRenderer(final @NonNull AzArmorRendererConfig config, final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        super(config);
        this.model = model;
        this.texture = texture;
    }

    /**
     * Constructs a renderer with a fully customizable config and model/texture paths based on the specified ID.
     *
     * <ul>
     * <li>Resulting model path will be equivalent to <code>JCraft.id("geo/" + id + ".geo.json")</code></li>
     * <li>Resulting texture path will be equivalent to <code>JCraft.id("textures/armor/" + id + ".png")</code></li>
     * </ul>
     */
    protected ArmorRenderer(final @NonNull AzArmorRendererConfig config, final @NonNull String id) {
        this(config, JCraft.id(MODEL_STR_TEMPLATE.formatted(id)), JCraft.id(TEXTURE_STR_TEMPLATE.formatted(id)));
    }

    /**
     * Constructs a renderer with a simple config based on the {@link AzAnimator} {@link Supplier} and the given model/texture paths.
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        this(animatorSupplier, UnaryOperator.identity(), model, texture);
    }

    /**
     * Constructs a renderer with a simple config based on the {@link AzAnimator} {@link Supplier} and model/texture paths based on the specified ID.
     *
     * <ul>
     * <li>Resulting model path will be equivalent to <code>JCraft.id("geo/" + id + ".geo.json")</code></li>
     * <li>Resulting texture path will be equivalent to <code>JCraft.id("textures/armor/" + id + ".png")</code></li>
     * </ul>
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull String id) {
        this(animatorSupplier, JCraft.id(MODEL_STR_TEMPLATE.formatted(id)), JCraft.id(TEXTURE_STR_TEMPLATE.formatted(id)));
    }

    /**
     * Constructs a renderer with a config based on the {@link AzAnimator} {@link Supplier}, the given bone context and the {@link mod.azure.azurelib.render.entity.AzEntityRendererConfig.Builder} {@link Function}, and the given model/texture paths based on the specified ID.
     *
     * <ul>
     * <li>Resulting model path will be equivalent to <code>JCraft.id("geo/" + id + ".geo.json")</code></li>
     * <li>Resulting texture path will be equivalent to <code>JCraft.id("textures/armor/" + id + ".png")</code></li>
     * </ul>
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull AzArmorBoneContext boneContext, final @NonNull Function<AzArmorRendererConfig.Builder, AzArmorRendererConfig.Builder> additionalConfigs, final @NonNull String id) {
        this(animatorSupplier, boneContext, additionalConfigs, JCraft.id(MODEL_STR_TEMPLATE.formatted(id)), JCraft.id(TEXTURE_STR_TEMPLATE.formatted(id)));
    }

    /**
     * Constructs a renderer with a config based on the {@link AzAnimator} {@link Supplier} and the given bone context, and the given model/texture paths based on the specified ID.
     *
     * <ul>
     * <li>Resulting model path will be equivalent to <code>JCraft.id("geo/" + id + ".geo.json")</code></li>
     * <li>Resulting texture path will be equivalent to <code>JCraft.id("textures/armor/" + id + ".png")</code></li>
     * </ul>
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull AzArmorBoneContext boneContext, final @NonNull String id) {
        this(animatorSupplier, boneContext, UnaryOperator.identity(), JCraft.id(MODEL_STR_TEMPLATE.formatted(id)), JCraft.id(TEXTURE_STR_TEMPLATE.formatted(id)));
    }

    /**
     * Constructs a renderer with a config based on the {@link AzAnimator} {@link Supplier} and the given bone context, and the given model/texture paths.
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull AzArmorBoneContext boneContext, final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        this(animatorSupplier, boneContext, UnaryOperator.identity(), model, texture);
    }

    /**
     * Constructs a renderer with a config based on the {@link AzAnimator} {@link Supplier} and the {@link mod.azure.azurelib.render.entity.AzEntityRendererConfig.Builder} {@link Function}, and the given model/texture paths.
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull Function<AzArmorRendererConfig.Builder, AzArmorRendererConfig.Builder> additionalConfigs, final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        this(animatorSupplier, new AzArmorBoneContext(), additionalConfigs, model, texture);
    }

    /**
     * Constructs a renderer with a config based on the {@link AzAnimator} {@link Supplier}, given bone context and the {@link mod.azure.azurelib.render.entity.AzEntityRendererConfig.Builder} {@link Function}, and the given model/texture paths.
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull AzArmorBoneContext boneContext, final @NonNull Function<AzArmorRendererConfig.Builder, AzArmorRendererConfig.Builder> additionalConfigs, final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        this(additionalConfigs.apply(
                AzArmorRendererConfig.builder(model, texture)
                        .setAnimatorProvider(animatorSupplier)
                        .setBoneProvider(BONE_PROVIDER)
                        .setPrerenderEntry(ArmorRenderer::scaleBonesForSlimModel)
                        .setPipelineContext(pipeline -> new AzArmorRendererPipelineContext(pipeline) {
                            private final AzArmorBoneContext ourContext = boneContext;

                            @Override
                            public AzArmorBoneContext boneContext() {
                                return ourContext;
                            }
                        })
                )
                .build(), model, texture);
    }

    /**
     * Constructs a renderer with a config based on the {@link AzAnimator} {@link Supplier} and the {@link mod.azure.azurelib.render.entity.AzEntityRendererConfig.Builder} {@link Function}, and model/texture paths based on the specified ID.
     *
     * <ul>
     * <li>Resulting model path will be equivalent to <code>JCraft.id("geo/" + id + ".geo.json")</code></li>
     * <li>Resulting texture path will be equivalent to <code>JCraft.id("textures/armor/" + id + ".png")</code></li>
     * </ul>
     */
    protected ArmorRenderer(final @NonNull Supplier<AzAnimator<UUID, ItemStack>> animatorSupplier, final @NonNull Function<AzArmorRendererConfig.Builder, AzArmorRendererConfig.Builder> additionalConfigs, final @NonNull String id) {
        this(animatorSupplier, additionalConfigs, JCraft.id(MODEL_STR_TEMPLATE.formatted(id)), JCraft.id(TEXTURE_STR_TEMPLATE.formatted(id)));
    }

    /**
     * Returns a supplier for a simple {@link ArmorRenderer}, only identified by its ID.
     */
    public static Supplier<AzArmorRenderer> simple(final String id) {
        return () -> new ArmorRenderer(() -> new ArmorAnimator(id), id);
    }

    /**
     * Returns a supplier for a simple fluttering {@link ArmorRenderer}, only identified by its ID.
     */
    public static Supplier<AzArmorRenderer> flutter(final String id) {
        return () -> new ArmorRenderer(() -> new FlutteringArmorAnimator(id), id);
    }

    private static AzRendererPipelineContext<UUID, ItemStack> scaleBonesForSlimModel(AzRendererPipelineContext<UUID, ItemStack> ctx) {
        // Scale the arms to 3/4 of their original size for slim models.
        // Slim models have 3 pixel wide arms rather than 4 pixel wide arms

        // We use this convoluted method to check if the player's model is slim
        // because I don't trust comparing player.getModelName() to "slim"
        // as it may not always be accurate in combination with other mods or future updates.
        Entity entity = ctx.currentEntity();
        AzBakedModel model = ctx.bakedModel();

        if (!(entity instanceof AbstractClientPlayer player)) return ctx;
        EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);

        if (!(renderer instanceof PlayerRenderer playerRenderer)) return ctx;
        PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();

        if (!((PlayerModelAccessor) playerModel).isSlim())
            return ctx;

        AzBone leftArm = BONE_PROVIDER.getLeftArmBone(model);
        AzBone rightArm = BONE_PROVIDER.getRightArmBone(model);

        if (leftArm != null && rightArm != null) {
            leftArm.setScaleX(0.75f);
            leftArm.setPivotX(-4.5f); // Default pivot is -5
            rightArm.setScaleX(0.75f);
            rightArm.setPivotX(4.5f);
        }

        return ctx;
    }

    /**
     * Basic {@link AzItemAnimator} implementation that can be used or extended for all kinds of {@link net.minecraft.world.item.ArmorItem} animators.
     */
    public static class ArmorAnimator extends AzItemAnimator {

        /**
         * Path to the animation to be used for this entity.
         */
        protected final @NonNull ResourceLocation animation;

        /**
         * Constructs an animator with the given animation path.
         */
        public ArmorAnimator(final @NonNull ResourceLocation animation) {
            this.animation = animation;
        }

        /**
         * Constructs an animator with the given ID.
         * <p>
         * Resulting animation path will be equivalent to <code>JCraft.id("animations/" + id + ".animation.json")</code>
         */
        public ArmorAnimator(final @NonNull String id) {
            this(JCraft.id(ANIMATION_STR_TEMPLATE.formatted(id)));
        }

        @Override
        public void registerControllers(final @NonNull AzAnimationControllerContainer<ItemStack> animationControllerContainer) {
            animationControllerContainer.add(AzAnimationController.builder(this, JCraft.BASE_CONTROLLER).build());
        }

        @Override
        public @NotNull ResourceLocation getAnimationLocation(final @NonNull ItemStack animatable) {
            return animation;
        }
    }

    public static class FlutteringArmorAnimator extends ArmorAnimator {

        /**
         * Constructs a fluttering animator with the given animation path.
         */
        public FlutteringArmorAnimator(final @NonNull ResourceLocation animation) {
            super(animation);
        }

        /**
         * Constructs a fluttering animator with the given ID.
         * <p>
         * Resulting animation path will be equivalent to <code>JCraft.id("animations/" + id + ".animation.json")</code>
         */
        public FlutteringArmorAnimator(final @NonNull String id) {
            super(id);
        }

        @Override
        public void registerControllers(final @NonNull AzAnimationControllerContainer<ItemStack> animationControllerContainer) {
            animationControllerContainer.add(AzAnimationController.builder(this, JCraft.BASE_CONTROLLER).setTransitionLength(10).build());
        }

    }
}
