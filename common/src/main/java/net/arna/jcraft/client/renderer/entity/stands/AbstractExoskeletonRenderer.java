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
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.renderer.entity.StandEntityModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

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

        @Override
        protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks,
                                      float rotationYaw, float partialTick, float nativeScale) {
            super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
            LivingEntity user = animatable.getUser();
            if (user == null) return;
            if (user.isCrouching()) {
                poseStack.translate(0.0F, user.getScale() * -0.125F, 0.0F);
            } else if (user.isVisuallySwimming()) {
                float swimAngle = user instanceof Player ? -90.0F : -45.0F;
                poseStack.mulPose(Axis.XP.rotationDegrees(
                        Mth.lerp(user.getSwimAmount(partialTick), 0.0F, swimAngle)));
                poseStack.translate(0.0F, -1.0F, 0.0F);
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
            boneContext.grabRelevantBones(bakedModel, boneProvider);
            boneContext.applyBaseTransformations(ownerModel);
        }
    }
}
