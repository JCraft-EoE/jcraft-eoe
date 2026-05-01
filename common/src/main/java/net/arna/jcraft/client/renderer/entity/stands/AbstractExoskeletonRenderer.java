package net.arna.jcraft.client.renderer.entity.stands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneProvider;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import mod.azure.azurelib.util.client.RenderUtils;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.renderer.entity.StandEntityModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

/**
 * Renderer base for stands that mimic the user's pose (currently TCB and Mandom).
 * <p>
 * Conceptually the exoskeleton is "the user with extra geometry": every frame the visible pose of the
 * stand needs to match the visible pose of the user as closely as possible. There are two layers of
 * matching going on:
 * <ol>
 *   <li><b>Per-bone:</b> {@link ExoskeletonAnimator#setCustomAnimations} grabs the user's
 *       {@link HumanoidModel} via its renderer and copies the per-part rotations / positions into our
 *       {@link AzBone bones}. That handles things like limb swing, the crouch body lower, the swim
 *       limb pose, etc. - all the things {@link HumanoidModel#setupAnim} does to the user's bones.</li>
 *   <li><b>Whole-model:</b> {@link ExoskeletonModelRenderer#applyRotations(StandEntity, PoseStack, float, float, float, float)}
 *       mirrors the global pose-stack
 *       transforms that LivingEntityRenderer#setupRotations(LivingEntity, PoseStack, float, float, float)
 *       (and the Player override of it) apply to the user. That covers death tilt, sleeping, upside-down,
 *       fall-flying tilt, and the big -90&deg; swim/crawl tilt + offset that the user's renderer applies.</li>
 * </ol>
 * The previous implementation only did a tiny ad-hoc subset of the second layer (a hard-coded crouch
 * translate and a constant swim tilt that ignored the user's pitch) which is why crawling sat too low,
 * swim-pitch never tracked the user, fall-flying didn't tilt at all, and pose transitions snapped instead
 * of lerping.
 */
public abstract class AbstractExoskeletonRenderer<T extends StandEntity<?, ?>> extends StandEntityRenderer<T> {

    public static final AzArmorBoneProvider DEFAULT_BONE_PROVIDER = new AzArmorBoneProvider() {
        @Override public AzBone getHeadBone(AzBakedModel m)     { return m.getBoneOrNull("helmet"); }
        @Override public AzBone getBodyBone(AzBakedModel m)     { return m.getBoneOrNull("chestplate"); }
        @Override public AzBone getRightArmBone(AzBakedModel m) { return m.getBoneOrNull("rightArm"); }
        @Override public AzBone getLeftArmBone(AzBakedModel m)  { return m.getBoneOrNull("leftArm"); }
        @Override public AzBone getRightLegBone(AzBakedModel m) { return m.getBoneOrNull("rightLeg"); }
        @Override public AzBone getLeftLegBone(AzBakedModel m)  { return m.getBoneOrNull("leftLeg"); }
        @Override public AzBone getRightBootBone(AzBakedModel m){ return m.getBoneOrNull("rightBoot"); }
        @Override public AzBone getLeftBootBone(AzBakedModel m) { return m.getBoneOrNull("leftBoot"); }
        @Override public AzBone getWaistBone(AzBakedModel m)    { return null; }
    };

    protected AbstractExoskeletonRenderer(EntityRendererProvider.Context context, StandType standType) {
        this(context, Function.identity(), standType, false, false, 0f, 0f, 1f, DEFAULT_BONE_PROVIDER);
    }

    protected AbstractExoskeletonRenderer(EntityRendererProvider.Context context, StandType standType,
                                          AzArmorBoneProvider boneProvider) {
        this(context, Function.identity(), standType, false, false, 0f, 0f, 1f, boneProvider);
    }

    protected AbstractExoskeletonRenderer(
            EntityRendererProvider.Context context,
            Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> configurer,
            StandType standType,
            boolean flipBody, boolean flipHead,
            float torsoPitchOffset, float headPitchOffset, float velInfluence
    ) {
        this(context, configurer, standType, flipBody, flipHead,
                torsoPitchOffset, headPitchOffset, velInfluence, DEFAULT_BONE_PROVIDER);
    }

    protected AbstractExoskeletonRenderer(
            EntityRendererProvider.Context context,
            Function<AzEntityRendererConfig.Builder<T>, AzEntityRendererConfig.Builder<T>> configurer,
            StandType standType,
            boolean flipBody, boolean flipHead,
            float torsoPitchOffset, float headPitchOffset, float velInfluence,
            AzArmorBoneProvider boneProvider
    ) {
        super(
                context,
                builder -> configurer.apply(
                        builder
                                .setAnimatorProvider(() -> new ExoskeletonAnimator<>(
                                        standType.getId().getPath(),
                                        flipBody, flipHead,
                                        torsoPitchOffset, headPitchOffset, velInfluence,
                                        boneProvider
                                ))
                                .setModelRenderer(ExoskeletonModelRenderer::new)
                ),
                standType, flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence
        );
    }

    public static class ExoskeletonModelRenderer<T extends StandEntity<?, ?>> extends StandEntityModelRenderer<T> {

        public ExoskeletonModelRenderer(AzRendererPipeline<UUID, T> pipeline,
                                        AzLayerRenderer<UUID, T> layerRenderer) {
            super(pipeline, layerRenderer);
        }

        /**
         * Replaces (rather than augments) the parent's {@code applyRotations} so that every transform is
         * driven off the <b>user</b> rather than the stand. The parent would, for example, rotate around
         * the stand's death-time / auto-spin / sleeping pose, none of which match the user's actual
         * visual state.
         */
        @Override
        protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks,
                                      float rotationYaw, float partialTick, float nativeScale) {
            LivingEntity user = animatable.getUser();
            if (user == null) {
                super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
                return;
            }

            // Mirror LivingEntityRenderer.setupRotations (frozen wobble, body Y, death/spin/sleep/upside-down).
            applyLivingRotations(user, poseStack, rotationYaw, partialTick, nativeScale);
            // Mirror PlayerRenderer.setupRotations on top (fall-flying tilt + yaw, swim/crawl tilt + offset).
            applySwimAndFlightRotations(user, poseStack, partialTick);
            // Crouch fixup. The HumanoidModel.setupAnim crouch code lowers body/arms/legs/head by their
            // own Y offsets, and applyBaseTransformations propagates those into our bones - but vanilla's
            // body part pivots aren't always at the same place as the bedrock bone pivots (vanilla pivots
            // around the neck; bedrock chestplate bones sometimes pivot mid-torso), so the same xRot
            // produces a slightly different result and the geometry sits a bit too high / leaned back.
            // This empirical offset, originally in the previous implementation, is what the model artist
            // calibrated against. Gated on isCrouching alone (mutually exclusive with the swim/fall poses
            // above) to mirror the original behavior.
            if (user.isCrouching()) {
                poseStack.translate(0f, user.getScale() * -0.125f, 0f);
            }
        }

        /**
         * Faithful port of {@code LivingEntityRenderer.setupRotations} for a 1.20.1 user.
         */
        private static void applyLivingRotations(LivingEntity user, PoseStack poseStack,
                                                 float rotationYaw, float partialTick, float nativeScale) {
            if (user.isFullyFrozen()) {
                rotationYaw += (float) (Math.cos(user.tickCount * 3.25d) * Math.PI * 0.4d);
            }

            if (!user.hasPose(Pose.SLEEPING)) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180f - rotationYaw));
            }

            if (user.deathTime > 0) {
                float deathRotation = (user.deathTime + partialTick - 1f) / 20f * 1.6f;
                poseStack.mulPose(Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1f) * 90f));
            } else if (user.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f - user.getXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees((user.tickCount + partialTick) * -75f));
            } else if (user.hasPose(Pose.SLEEPING)) {
                Direction bedOrientation = user.getBedOrientation();
                poseStack.mulPose(Axis.YP.rotationDegrees(
                        bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
                poseStack.mulPose(Axis.YP.rotationDegrees(270f));
            } else if (LivingEntityRenderer.isEntityUpsideDown(user)) {
                poseStack.translate(0, (user.getBbHeight() + 0.1f) / nativeScale, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            }
        }

        /**
         * Faithful port of the player-specific bits of {@code PlayerRenderer.setupRotations} - the swim
         * tilt (also used for crawling, since both share Pose.SWIMMING) and the elytra fall-flying tilt.
         * <p>
         * Kept gating on {@code swimAmount > 0} (vs {@code isVisuallySwimming()}) so the rotation lerps
         * during the few ticks that swimAmount takes to ramp 0->1 and back, which is how the user's own
         * renderer behaves and is the source of the smooth pose transition the user is comparing against.
         * <p>
         * For non-player living entities we still get the swim tilt - they can be in Pose.SWIMMING too -
         * but skip fall-flying which is player-only.
         */
        private static void applySwimAndFlightRotations(LivingEntity user, PoseStack poseStack, float partialTick) {
            float swimAmount = user.getSwimAmount(partialTick);
            float pitch = user.getViewXRot(partialTick);

            if (user instanceof Player player && player.isFallFlying()) {
                float fallFlyTicks = (float) player.getFallFlyingTicks() + partialTick;
                float clampedFly = Mth.clamp(fallFlyTicks * fallFlyTicks / 100f, 0f, 1f);
                if (!player.isAutoSpinAttack()) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(clampedFly * (-90f - pitch)));
                }
                Vec3 viewVec = player.getViewVector(partialTick);
                Vec3 deltaVec = player.getDeltaMovement();
                double horizontalDeltaSq = deltaVec.horizontalDistanceSqr();
                double horizontalViewSq = viewVec.horizontalDistanceSqr();
                if (horizontalDeltaSq > 0 && horizontalViewSq > 0) {
                    double dot = (deltaVec.x * viewVec.x + deltaVec.z * viewVec.z)
                            / Math.sqrt(horizontalDeltaSq * horizontalViewSq);
                    double cross = deltaVec.x * viewVec.z - deltaVec.z * viewVec.x;
                    poseStack.mulPose(Axis.YP.rotation((float) (Math.signum(cross) * Math.acos(dot))));
                }
            } else if (swimAmount > 0f) {
                // In water we follow the user's pitch so the stand rolls up/down as the player looks
                // around; on land (crawling) the user is locked horizontal so we are too.
                float swimAngle = user.isInWater() ? -90f - pitch : -90f;
                float lerpedSwim = Mth.lerp(swimAmount, 0f, swimAngle);
                poseStack.mulPose(Axis.XP.rotationDegrees(lerpedSwim));
                if (user.isVisuallySwimming()) {
                    // Vanilla's PlayerRenderer translates by (0, -1, 0.3); the previous code dropped the
                    // 0.3 which in the post-rotation frame is 0.3 of vertical lift, hence the
                    // "exoskeleton sits too low while crawling" complaint.
                    poseStack.translate(0f, -1f, 0.3f);
                }
            }
        }
    }

    public static class ExoskeletonAnimator<T extends StandEntity<?, ?>> extends StandEntityRenderer.StandAnimator<T> {

        private final AzArmorBoneProvider boneProvider;
        private final AzArmorBoneContext boneContext = new AzArmorBoneContext();

        public ExoskeletonAnimator(
                String animationPath,
                boolean flipBody, boolean flipHead,
                float torsoPitchOffset, float headPitchOffset, float velInfluence,
                AzArmorBoneProvider boneProvider
        ) {
            super(animationPath, flipBody, flipHead, torsoPitchOffset, headPitchOffset, velInfluence);
            this.boneProvider = boneProvider;
        }

        @Override
        public void setCustomAnimations(@NotNull T entity, float partialTick) {
            LivingEntity user = entity.getUser();
            if (user == null) return;
            EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(user);
            if (!(renderer instanceof LivingEntityRenderer<?, ?> livingRenderer)) return;
            if (!(livingRenderer.getModel() instanceof HumanoidModel<?> ownerModel)) return;
            AzBakedModel bakedModel = context().boneCache().getBakedModel();
            if (bakedModel == null) return;

            // The stand and the user are independent entities, so the engine doesn't promise a render
            // order between them. If the stand is rendered first this frame, the user's HumanoidModel
            // still holds *last frame's* setupAnim output, which is what made pose transitions look
            // jumpy compared to the user (the user is always animated at "now"; we'd be at "now-1").
            // Re-running setupAnim ourselves here costs one extra animation pass per frame in exchange
            // for the stand always being one frame in sync with the user.
            refreshHumanoidPose(ownerModel, user, partialTick);

            boneContext.grabRelevantBones(bakedModel, boneProvider);
            boneContext.applyBaseTransformations(ownerModel);
        }

        /**
         * Re-runs {@link HumanoidModel#setupAnim} with the same parameters the user's own renderer
         * computes, ensuring the model parts we are about to copy from reflect the current frame.
         * The cast is unchecked because {@code HumanoidModel<T extends LivingEntity>} doesn't expose
         * its bound here, but the renderer that owns the model is by construction the one that
         * accepts {@code user}, so the call is safe; we still defend with a try/catch in case a third
         * party plugs in a renderer with surprising expectations.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static void refreshHumanoidPose(HumanoidModel<?> model, LivingEntity user, float partialTick) {
            try {
                float limbSwing = user.walkAnimation.position(partialTick);
                float limbSwingAmount = Math.min(user.walkAnimation.speed(partialTick), 1.0f);
                float ageInTicks = user.tickCount + partialTick;
                float netHeadYaw = Mth.rotLerp(partialTick, user.yHeadRotO, user.yHeadRot)
                                 - Mth.rotLerp(partialTick, user.yBodyRotO, user.yBodyRot);
                float headPitch = Mth.lerp(partialTick, user.xRotO, user.getXRot());

                model.attackTime = user.getAttackAnim(partialTick);
                // Vanilla LivingEntityRenderer ANDs in vehicle.shouldRiderSit() here, but that method
                // doesn't exist in 1.20.1 mappings; AzEntityModelRenderer drops it for the same reason
                // and the only consequence is that the model's riding-pose arm tweak fires for any
                // mounted user instead of just sitting ones, which is a non-issue for the exoskeleton.
                model.riding = user.isPassenger() && user.getVehicle() != null;
                model.young = user.isBaby();
                model.crouching = user.isCrouching();
                model.swimAmount = user.getSwimAmount(partialTick);

                ((HumanoidModel) model).setupAnim(user, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            } catch (Throwable ignored) {
                // Fall back to whatever pose the model already had - worst case we are one frame stale,
                // which is what the previous implementation always was.
            }
        }
    }
}
