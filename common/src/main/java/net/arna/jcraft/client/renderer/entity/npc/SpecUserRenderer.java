package net.arna.jcraft.client.renderer.entity.npc;

import lombok.NonNull;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpecUserRenderer<T extends SpecUserMob> extends AbstractEntityRenderer<T> {

    protected static final ResourceLocation GENERIC_ANIMATIONS = JCraft.id("animations/spec/spec_user.animation.json");

    public SpecUserRenderer(final @NonNull EntityRendererProvider.Context context, final @NonNull ResourceLocation model, final @NonNull ResourceLocation texture) {
        super(context, () -> new SpecUserAnimator<>(GENERIC_ANIMATIONS),
                b -> b
                        .addRenderLayer(new HandItemsRenderLayer<>())
                        .setRenderEntry((pc) -> {
                            pc.animatable().updateAnimations();
                            return pc;
                        }),
                model,
                texture
        );
    }

    private static class SpecUserAnimator<T extends SpecUserMob> extends EntityAnimator<T> {
        public SpecUserAnimator(@NonNull ResourceLocation animation) {
            super(animation);
        }

        @Override
        public void registerControllers(@NonNull AzAnimationControllerContainer<T> animationControllerContainer) {
            animationControllerContainer.add(AzAnimationController.builder(this, SpecUserMob.MOVEMENT_CONTROLLER).setTransitionLength(0).build());
            animationControllerContainer.add(AzAnimationController.builder(this, JCraft.BASE_CONTROLLER).setTransitionLength(0).build());
        }

        @Override
        public void setCustomAnimations(final T animatable, final float partialTicks) {
            final AzBakedModel model = context().boneCache().getBakedModel();
            final AzBone head = model.getBoneOrNull("head");

            if (head != null) {
                head.setRotX(-animatable.getXRot() * Mth.DEG_TO_RAD);
                head.setRotY((animatable.getYRot() - animatable.getViewYRot(partialTicks)) * Mth.DEG_TO_RAD);
            }

            /*
            final AzBone leftLeg = model.getBoneOrNull("leftLeg");
            final AzBone rightLeg = model.getBoneOrNull("rightLeg");
            final AzBone leftArm = model.getBoneOrNull("leftArm");
            final AzBone rightArm = model.getBoneOrNull("rightArm");
             */
        }
    }

    // TODO: armor rendering support

    /*
    public SpecUserRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);

        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(final GeoBone bone, final T animatable) {
                // Retrieve the items in the entity's hands for the relevant bone
                return switch (bone.getName()) {
                    case LEFT_HAND -> animatable.isLeftHanded() ?
                            mainHandItem : offHandItem;
                    case RIGHT_HAND -> animatable.isLeftHanded() ?
                            offHandItem : mainHandItem;
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(final GeoBone bone, final ItemStack stack, final T animatable) {
                // Apply the camera transform for the given hand
                return switch (bone.getName()) {
                    case LEFT_HAND, RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            // Do some quick render modifications depending on what the item is
            @Override
            protected void renderStackForBone(final PoseStack poseStack, final GeoBone bone, final ItemStack stack, final T animatable,
                                              final MultiBufferSource bufferSource, final float partialTick, final int packedLight, final int packedOverlay) {

                if (stack == mainHandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0, 0.125, -0.25);
                    }
                } else if (stack == offHandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0, 0.125, 0.25);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.mainHandItem = animatable.getMainHandItem();
        this.offHandItem = animatable.getOffhandItem();

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }*/
}
