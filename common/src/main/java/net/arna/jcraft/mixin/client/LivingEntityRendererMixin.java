package net.arna.jcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.rendering.AlphaFadeBufferSource;
import net.arna.jcraft.client.rendering.MihAfterimageTrail;
import net.arna.jcraft.client.renderer.features.ArmoredMoveFeatureRenderer;
import net.arna.jcraft.client.renderer.features.HamonParticlesFeatureRenderer;
import net.arna.jcraft.client.renderer.features.StuckKnivesFeatureRenderer;
import net.arna.jcraft.client.util.PlayerCloneClientPlayerEntity;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    @Shadow
    protected M model;

    @Shadow
    @Final
    protected List<RenderLayer<T, M>> layers;

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At("RETURN"), method = "<init>")
    private void addFeatureRenderers(EntityRendererProvider.Context ctx, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        if (model instanceof AgeableListModel<?>)
        {
            // Stuck Knives
            addLayer((RenderLayer<T, M>) new StuckKnivesFeatureRenderer<>(ctx, (LivingEntityRenderer<T, ? extends AgeableListModel<T>>) (Object) this));
            // Hamon Particles
            addLayer((RenderLayer<T, M>) new HamonParticlesFeatureRenderer<>(ctx, (LivingEntityRenderer<T, ? extends AgeableListModel<T>>) (Object) this));
        }
        if (model != null) {
            addLayer((RenderLayer<T, M>) new ArmoredMoveFeatureRenderer<>(ctx, (LivingEntityRenderer<T, ? extends EntityModel<T>>) (Object) this));
        }
    }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void doNotRenderCloneLabel(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof PlayerCloneClientPlayerEntity || jcraft$renderingAfterimage) {
            cir.setReturnValue(false); // suppress name tags on the afterimage copies too
        }
    }

    // Made In Heaven acceleration afterimage: re-render the whole entity (model, armor, held items, all layers) along
    // the path it actually travelled while ramping, fading down the trail. Renderer-level rather than a layer so armor
    // is included; faded translucent via AlphaFadeBufferSource. The copy count scales with the ramp.
    @Unique
    private boolean jcraft$renderingAfterimage = false;

    @SuppressWarnings("unchecked")
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void jcraft$renderAfterimages(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                                          MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (jcraft$renderingAfterimage) {
            return; // we're inside a copy already; don't spawn more (prevents infinite recursion)
        }
        if (entity.isInvisible() || !(JUtils.getStand(entity) instanceof MadeInHeavenEntity mih)) {
            return;
        }
        final float intensity = mih.getRampIntensity();

        // Record where the user is this tick, then draw copies along the recent path.
        final MihAfterimageTrail trail = MihAfterimageTrail.get(entity, MIH_AFTERIMAGE_MAX_COPIES + 2);
        trail.sample(entity.tickCount, entity.position());

        final int copies = Mth.ceil(intensity * MIH_AFTERIMAGE_MAX_COPIES);
        if (copies <= 0) {
            return;
        }
        final Vec3 current = entity.getPosition(partialTicks); // smoothed render position this frame

        jcraft$renderingAfterimage = true;
        for (int k = copies; k >= 1; k--) { // farthest first so nearer copies/the real body draw on top
            final Vec3 older = trail.at(k + 1);
            final Vec3 newer = trail.at(k);
            if (older == null || newer == null) {
                continue; // trail not long enough yet (just started ramping) -> copies grow in
            }
            // Interpolate between two samples so the copy flows out of the player instead of stepping once per tick.
            final double t = partialTicks;
            final double dx = ((older.x + (newer.x - older.x) * t) - current.x) * MIH_AFTERIMAGE_SPREAD;
            final double dy = ((older.y + (newer.y - older.y) * t) - current.y) * MIH_AFTERIMAGE_SPREAD;
            final double dz = ((older.z + (newer.z - older.z) * t) - current.z) * MIH_AFTERIMAGE_SPREAD;

            // Fade down the trail: nearest copy strongest, farthest faintest.
            final float fade = MIH_AFTERIMAGE_BASE_ALPHA * (copies - k + 1) / (float) copies;
            final int alpha = Mth.clamp((int) (fade * 255f), 0, 255);

            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            ((LivingEntityRenderer<T, M>) (Object) this).render(entity, entityYaw, partialTicks, poseStack,
                    new AlphaFadeBufferSource(buffer, alpha), light);
            poseStack.popPose();
        }
        jcraft$renderingAfterimage = false;
    }

    @Unique
    private static final int MIH_AFTERIMAGE_MAX_COPIES = 8; // max copies at full ramp
    @Unique
    private static final double MIH_AFTERIMAGE_SPREAD = 1.0; // >1 exaggerates spacing along the path
    @Unique
    private static final float MIH_AFTERIMAGE_BASE_ALPHA = 0.55f; // alpha of the nearest copy

    /*
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void suckmahballs(T livingEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
        if (true || !(JUtils.getStand((LivingEntity) (Object) this) instanceof KingCrimsonEntity kc) || kc.getState() != KingCrimsonEntity.State.PREDICT ||
                kc.getMoveStun() > (KingCrimsonEntity.PREDICTION.getWindupPoint())) {
            return;
        }

        RenderType renderLayer = JRenderLayerRegistry.RRRE;
        if (renderLayer != null) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            int o = LivingEntityRenderer.getOverlayCoords(livingEntity, this.getBob(livingEntity, g));
            this.model.renderToBuffer(matrixStack, vertexConsumer, i, o, 1, 1, 1, 1);
        }

        for (RenderLayer<T, M> featureRenderer : layers) {

        }
    }
     */

    @Shadow
    protected abstract float getBob(T entity, float tickDelta);

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> feature);
}
