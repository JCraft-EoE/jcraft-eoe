package net.arna.jcraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import mod.azure.azurelib.animation.AzAnimatorConfig;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.render.block.AzBlockEntityRenderer;
import mod.azure.azurelib.render.block.AzBlockEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.tile.CoffinTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CoffinTileRenderer extends AzBlockEntityRenderer<CoffinTileEntity> {

    public static final ResourceLocation GEO = JCraft.id("geo/coffin.geo.json");
    public static final ResourceLocation TEXTURE = JCraft.id("textures/block/coffin.png");
    public static final ResourceLocation ANIMATION = JCraft.id("animations/coffin.animation.json");

    public CoffinTileRenderer() {
        super(AzBlockEntityRendererConfig.<CoffinTileEntity>builder(GEO, TEXTURE)
                .setAnimatorProvider(CoffinAnimator::new)
                .build());
    }

    @Override
    public void render(final @NonNull CoffinTileEntity animatable, final float partialTick, final @NonNull PoseStack poseStack,
                       final @NonNull MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        if (BedPart.HEAD == animatable.getBlockState().getValue(BedBlock.PART)) {
            return;
        }
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    public static class CoffinAnimator extends AzBlockAnimator<CoffinTileEntity> {

        public CoffinAnimator() {
            super(AzAnimatorConfig.defaultConfig());
        }

        @Override
        public void registerControllers(AzAnimationControllerContainer<CoffinTileEntity> animationControllerContainer) {
            animationControllerContainer.add(AzAnimationController.builder(this, JCraft.BASE_CONTROLLER).setTransitionLength(30).build());
        }

        @Override
        public @NotNull ResourceLocation getAnimationLocation(CoffinTileEntity animatable) {
            return ANIMATION;
        }
    }
}