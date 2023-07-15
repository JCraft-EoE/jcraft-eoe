package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.registry.JRenderLayerRegistry;
import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    @Shadow protected M model;

    @Shadow protected abstract float getAnimationProgress(T entity, float tickDelta);

    @Shadow @Final protected List<FeatureRenderer<T, M>> features;

    protected LivingEntityRenderMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void suckmahballs(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        if (false && MinecraftClient.getInstance().player instanceof IEntityDataSaver entityDataSaver) {
            if (entityDataSaver.getStand() instanceof KingCrimsonEntity kc && kc.getState() == 12 && kc.getMoveStun() <= (KingCrimsonEntity.prediction.moveStun - KingCrimsonEntity.prediction.initTime)) {
                RenderLayer renderLayer = JRenderLayerRegistry.RRRE;
                if (renderLayer != null) {
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                    int o = LivingEntityRenderer.getOverlay(livingEntity, this.getAnimationProgress(livingEntity, g));
                    this.model.render(matrixStack, vertexConsumer, i, o, 1, 1, 1, 1);
                }

                for (FeatureRenderer<T, M>  featureRenderer : features) {
                    //TODO we got all funny features here planet suckondeeze

                }
            }
        }
    }
}
