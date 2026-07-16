package net.arna.jcraft.client.renderer.item;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class PeacemakerAnimator extends AzItemAnimator {
    private static final ResourceLocation ANIMATION = JCraft.id("animations/peacemaker.animation.json");
    // Long enough to cover the flash keyframes in the fire animation, which scale it away again by
    // 0.16667s of the shot on their own.
    private static final long FIRE_EFFECT_NANOS = 375_000_000L;

    private AzAnimationController<ItemStack> controller;
    private UUID lastStackId;
    private long lastSequence;
    private boolean sequenceInitialized;
    private long fireEffectEndsAt;

    @Override
    public void registerControllers(AzAnimationControllerContainer<ItemStack> container) {
        controller = AzAnimationController.builder(this, JCraft.BASE_CONTROLLER).build();
        container.add(controller);
    }

    @Override
    public ResourceLocation getAnimationLocation(ItemStack stack) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(ItemStack stack, float partialTicks) {
        if (controller == null) {
            return;
        }

        // Read-only: this runs every frame, so it must not attach a tag to a freshly crafted gun.
        final CompoundTag data = stack.getTag();
        if (data == null) {
            return;
        }

        final UUID stackId = stackId(data);
        final long sequence = data.getLong(Peacemaker.ANIMATION_SEQUENCE_ID);

        // Adopt the sequence without playing anything the first time a stack is seen, otherwise
        // swapping to an already-fired revolver replays whatever it did last.
        if (!sequenceInitialized || (stackId != null && !stackId.equals(lastStackId))) {
            sequenceInitialized = true;
            lastStackId = stackId;
            lastSequence = sequence;
            return;
        }

        if (sequence == lastSequence) {
            return;
        }
        lastSequence = sequence;

        final String animation = data.getString(Peacemaker.ANIMATION_ID);
        if (animation.isEmpty()) {
            return;
        }

        // Working the trigger again has to restart the cycle rather than queue behind the last one.
        if ("cock".equals(animation)) {
            resetController();
        }

        // The flash only exists for the length of the shot; the rest of the time it stays hidden.
        fireEffectEndsAt = "fire".equals(animation) ? System.nanoTime() + FIRE_EFFECT_NANOS : 0L;

        // Every animation holds its last frame, matching how they are authored: without an explicit
        // behavior AzureLib ignores that and loops them instead. Holding also covers the gap until
        // the server marks the next stage, so nothing snaps back to the rest pose between them.
        dispatch(AzCommand.create(JCraft.BASE_CONTROLLER, animation, AzPlayBehaviors.HOLD_ON_LAST_FRAME));
    }

    public boolean isFireEffectVisible() {
        return System.nanoTime() < fireEffectEndsAt;
    }

    private void dispatch(AzCommand command) {
        command.actions().forEach(action -> action.handle(AzDispatchSide.CLIENT, this));
    }

    private void resetController() {
        controller.animationQueue().clear();
        controller.controllerTimer().reset();
        controller.keyframeManager().keyframeCallbackHandler().reset();
        controller.setCurrentAnimation(null);
        controller.stateMachine().stop();
    }

    private static UUID stackId(CompoundTag data) {
        return data.hasUUID(AzureLib.ITEM_UUID_TAG) ? data.getUUID(AzureLib.ITEM_UUID_TAG) : null;
    }
}
