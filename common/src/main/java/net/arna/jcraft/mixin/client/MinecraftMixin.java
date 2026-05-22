package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.arna.jcraft.client.pose.PoseWheelState;
import net.arna.jcraft.client.rendering.api.callbacks.DisplayResizeCallback;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Final private Window window;

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void onResizeDisplay(final CallbackInfo ci) {
        DisplayResizeCallback.EVENT.invoker().onResolutionChanged(window.getWidth(), window.getHeight());
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pickBlock()V"))
    private void ignorePickBlockWhenStandOut(Minecraft instance, Operation<Void> original) {
        if (JUtils.getStand(instance.player) == null)
            original.call(instance);
    }

    /**
     * When the pose wheel is open, consume hotbar key presses (1-9) and redirect them
     * to pose slot execution instead of switching the hotbar selection.
     */
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void jcraft$interceptHotbarForPoseWheel(CallbackInfo ci) {
        if (!PoseWheelState.isOpen()) return;
        Minecraft mc = (Minecraft) (Object) this;
        KeyMapping[] hotbarKeys = mc.options.keyHotbarSlots;
        for (int i = 0; i < hotbarKeys.length && i < 9; i++) {
            if (hotbarKeys[i].consumeClick()) {
                PoseWheelState.executeSlot(i);
            }
        }
    }
}
