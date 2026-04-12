package net.arna.jcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.NonNull;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.layer.AzBlockAndItemLayer;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GEButterflyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import java.util.UUID;

/**
 * The {@link AbstractEntityRenderer} for {@link GEButterflyEntity}.
 */
@Environment(EnvType.CLIENT)
public class GEButterflyRenderer extends AbstractEntityRenderer<GEButterflyEntity> {
    public static final String ID = "gebutterfly";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public GEButterflyRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE)
                .addRenderLayer(new GEButterflyRendererLayer()),
                ID);
    }

    protected static class GEButterflyRendererLayer extends AzBlockAndItemLayer<UUID, GEButterflyEntity> {
        protected ItemStack mainHandItem;

        @Override
        public void preRender(final AzRendererPipelineContext<UUID, GEButterflyEntity> context) {
            super.preRender(context);
            mainHandItem = context.animatable().getItemBySlot(EquipmentSlot.MAINHAND);
        }

        @Override
        public ItemStack itemStackForBone(final AzBone bone, final GEButterflyEntity animatable) {
            if (bone.getName().equals("base")) {
                return mainHandItem;
            }
            return null;
        }

        @Override
        protected void renderItemForBone(final AzRendererPipelineContext<UUID, GEButterflyEntity> context, final AzBone bone, final ItemStack itemStack, final GEButterflyEntity animatable) {
            final PoseStack poseStack = context.poseStack();
            if (itemStack == mainHandItem) {
                poseStack.scale(0.33f, 0.33f, 0.33f);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                if (itemStack.getItem() instanceof ShieldItem) {
                    poseStack.translate(0, 0.125, -0.25);
                }
            }
            super.renderItemForBone(context, bone, itemStack, animatable);
        }
    }

}
