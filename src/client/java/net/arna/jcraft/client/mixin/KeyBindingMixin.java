package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.util.TrackedKeyBinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @Shadow @Final private static Map<InputUtil.Key, KeyBinding> KEY_TO_BINDINGS;

    @Inject(method = "setKeyPressed", at = @At("HEAD"))
    private static void queueKeyPressOrRelease(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        KeyBinding binding = KEY_TO_BINDINGS.get(key);
        if (binding == null || pressed == binding.isPressed()) return;
        TrackedKeyBinding.onKeyPressSet(binding, pressed);
    }
}
