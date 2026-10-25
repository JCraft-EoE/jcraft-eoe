package net.arna.jcraft.fabric.mixin.client;

import net.arna.jcraft.client.input.TrackedKeyBinding;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyBindingMixin {

    @Inject(method = "setDown", at = @At("HEAD"))
    private void queueKeyPressOrRelease(boolean pressed, CallbackInfo ci) {
        KeyMapping binding = (KeyMapping) (Object) (this);
        if (pressed == binding.isDown()) {
            return;
        }
        TrackedKeyBinding.onKeyPressSet(binding, pressed);
    }
}
