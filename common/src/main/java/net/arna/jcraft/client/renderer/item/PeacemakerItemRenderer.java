package net.arna.jcraft.client.renderer.item;

import net.arna.jcraft.client.tracer.MuzzleTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import mod.azure.azurelib.render.item.AzItemRendererPipeline;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import net.arna.jcraft.JCraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Set;
import java.util.UUID;

public class PeacemakerItemRenderer extends AzItemRenderer {
    private static final ResourceLocation MODEL = JCraft.id("geo/peacemaker.geo.json");
    private static final ResourceLocation TEXTURE = JCraft.id("textures/item/peacemaker/peacemaker.png");
    private static final ResourceLocation EFFECTS_TEXTURE = JCraft.id("textures/item/peacemaker/effects.png");
    private static final RenderType EFFECTS_RENDER_TYPE = RenderType.entityTranslucentEmissive(EFFECTS_TEXTURE);
    private static final Set<String> EFFECT_BONES = Set.of("fire", "pressure");

    public PeacemakerItemRenderer() {
        super(AzItemRendererConfig.builder(MODEL, TEXTURE)
                .useNewOffset(true)
                .setAnimatorProvider(PeacemakerAnimator::new)
                // Animation state is keyed to the stack, so every place the same gun is drawn shares
                // one bone cache. Letting the inventory icon animate makes it drive those bones from
                // its own render pass and fight the held gun for them. Only the hands animate.
                .enableAnimationOnlyInContexts(
                        ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .setBoneTextureOverrideProvider(PeacemakerItemRenderer::effectTexture)
                .setBoneRenderTypeOverrideProvider(PeacemakerItemRenderer::effectRenderType)
                .build());
    }

    @Override
    protected AzItemRendererPipeline createPipeline(AzItemRendererConfig config) {
        return new AzItemRendererPipeline(config, this) {
            @Override
            public void preRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
                super.preRender(context, isReRender);
                hideMuzzleFlashWhenNotFiring(context);
            }

            @Override
            public void postRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
                super.postRender(context, isReRender);
                recordMuzzle(context, modelRenderTranslations);
            }
        };
    }

    private void recordMuzzle(AzRendererPipelineContext<UUID, ItemStack> context, Matrix4f modelRenderTranslations) {
        if (!(context instanceof AzItemRendererPipelineContext itemContext)) {
            return;
        }
        ItemDisplayContext displayContext = itemContext.getTransformType();
        if (displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        final AzBakedModel model = context.bakedModel();
        if (model == null) {
            return;
        }

        boolean rightContext = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean rightMainArm = minecraft.player.getMainArm() == HumanoidArm.RIGHT;
        InteractionHand hand = rightContext == rightMainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        model.getBone("effects").ifPresent(bone -> {
            Vector4f pivot = new Vector4f(
                    bone.getPivotX() / 16f,
                    bone.getPivotY() / 16f,
                    bone.getPivotZ() / 16f,
                    1f);
            modelRenderTranslations.transform(pivot);

            Camera camera = minecraft.gameRenderer.getMainCamera();
            Vec3 cameraPos = camera.getPosition();
            Vec3 look = minecraft.player.getLookAngle();
            org.joml.Vector3f upVector = camera.getUpVector();
            Vec3 up = new Vec3(upVector.x, upVector.y, upVector.z);
            Vec3 right = look.cross(up);
            Vec3 muzzle = cameraPos
                    .add(right.scale(pivot.x))
                    .add(up.scale(pivot.y))
                    .subtract(look.scale(pivot.z));
            MuzzleTracker.record(minecraft.player.getUUID(), hand, muzzle);
        });
    }

    /**
     * The animations scale the flash in and out themselves, but only once one has actually played:
     * a gun that has not animated yet sits at the model's own scale with the flash showing. Keeping
     * the parent hidden outside the shot covers that. Its children are left alone, so the fire
     * animation still owns the shape of the flash, and the cylinder is entirely the animations' to
     * drive; touching those bones here is what made the gun fight itself.
     */
    private void hideMuzzleFlashWhenNotFiring(AzRendererPipelineContext<UUID, ItemStack> context) {
        final AzBakedModel model = context.bakedModel();
        if (model == null) {
            return;
        }

        final boolean firing = getAnimator() instanceof PeacemakerAnimator animator && animator.isFireEffectVisible();
        model.getBone("effects").ifPresent(bone -> {
            bone.setHidden(!firing);
            bone.setChildrenHidden(!firing);
        });
    }

    private static ResourceLocation effectTexture(AzBone bone) {
        return EFFECT_BONES.contains(bone.getName()) ? EFFECTS_TEXTURE : null;
    }

    private static RenderType effectRenderType(AzBone bone) {
        return EFFECT_BONES.contains(bone.getName()) ? EFFECTS_RENDER_TYPE : null;
    }
}
