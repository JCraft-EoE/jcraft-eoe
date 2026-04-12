package net.arna.jcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzBlockAndItemLayer;
import net.arna.jcraft.common.entity.GESnakeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import java.util.UUID;

/**
 * The {@link AbstractEntityRenderer} for {@link GESnakeEntity}.
 */
@Environment(EnvType.CLIENT)
public class GESnakeRenderer extends AbstractEntityRenderer<GESnakeEntity> {
    
    private static final String ID = "gesnake";

    public GESnakeRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new SnakeAnimator(ID), b -> b
                        .addRenderLayer(new GESnakeRendererLayer())
                        .setPrerenderEntry((pc) -> {
                            final var snake = pc.animatable();

                            if (snake.moveAnalysis.isMoving()) {
                                GESnakeEntity.MOVE.sendForEntity(snake);
                            }

                            return pc;
                        }),
                ID);
    }

    protected static class SnakeAnimator extends EntityAnimator<GESnakeEntity> {
        public SnakeAnimator(@NonNull String id) {
            super(id);
        }

        @Override
        public void registerControllers(@NonNull AzAnimationControllerContainer<GESnakeEntity> animationControllerContainer) {
            animationControllerContainer.add(AzAnimationController.builder(this, GESnakeEntity.MOVEMENT_CONTROLLER).setTransitionLength(5).build());
            animationControllerContainer.add(AzAnimationController.builder(this, GESnakeEntity.ATTACK_CONTROLLER).setTransitionLength(0).build());
        }
    }

    protected static class GESnakeRendererLayer extends AzBlockAndItemLayer<UUID, GESnakeEntity> {
        protected ItemStack mainHandItem;

        @Override
        public void preRender(final AzRendererPipelineContext<UUID, GESnakeEntity> context) {
            super.preRender(context);
            mainHandItem = context.animatable().getItemBySlot(EquipmentSlot.MAINHAND);
        }

        @Override
        public ItemStack itemStackForBone(final AzBone bone, final GESnakeEntity animatable) {
            if (bone.getName().equals("body")) {
                return mainHandItem;
            }
            return null;
        }

        @Override
        protected void renderItemForBone(final AzRendererPipelineContext<UUID, GESnakeEntity> context, final AzBone bone, final ItemStack itemStack, final GESnakeEntity animatable) {
            final PoseStack poseStack = context.poseStack();
            if (itemStack == mainHandItem) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                if (itemStack.getItem() instanceof ShieldItem) {
                    poseStack.translate(0, 0.125, -0.25);
                }
            }
            super.renderItemForBone(context, bone, itemStack, animatable);
        }
    }
}
