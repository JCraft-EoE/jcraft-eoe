package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.registry.JRenderLayerRegistry;
import net.arna.jcraft.client.renderer.features.ArmoredMoveFeatureRenderer;
import net.arna.jcraft.client.renderer.features.StuckKnivesFeatureRenderer;
import net.arna.jcraft.client.util.PlayerCloneClientPlayerEntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    @Shadow protected M model;

    @Shadow @Final protected List<FeatureRenderer<T, M>> features;

    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At("RETURN"), method = "<init>")
    private void addFeatureRenderers(EntityRendererFactory.Context ctx, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        if (model instanceof AnimalModel<?>) // StuckKnives
            addFeature((FeatureRenderer<T, M>) new StuckKnivesFeatureRenderer<>(ctx, (LivingEntityRenderer<T, ? extends AnimalModel<T>>) (Object) this));
        if (model != null)
            addFeature((FeatureRenderer<T, M>) new ArmoredMoveFeatureRenderer<>(ctx, (LivingEntityRenderer<T, ? extends EntityModel<T>>) (Object) this ));
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void doNotRenderCloneLabel(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof PlayerCloneClientPlayerEntity) cir.setReturnValue(false);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void suckmahballs(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (true || !(JUtils.getStand((LivingEntity) (Object) this) instanceof KingCrimsonEntity kc) || kc.getState() != KingCrimsonEntity.State.PREDICT ||
                kc.getMoveStun() > (KingCrimsonEntity.PREDICTION.getWindupPoint()))
            return;

        RenderLayer renderLayer = JRenderLayerRegistry.RRRE;
        if (renderLayer != null) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            int o = LivingEntityRenderer.getOverlay(livingEntity, this.getAnimationProgress(livingEntity, g));
            this.model.render(matrixStack, vertexConsumer, i, o, 1, 1, 1, 1);
        }

        for (FeatureRenderer<T, M> featureRenderer : features) {
            //TODO we got all funny features here planet suckondeeze

        }
    }

    @Shadow protected abstract float getAnimationProgress(T entity, float tickDelta);

    @Shadow protected abstract boolean addFeature(FeatureRenderer<T, M> feature);
}
