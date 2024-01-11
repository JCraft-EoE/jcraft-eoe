package net.arna.jcraft.client.util;

import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * A KeyBinding that can tell you whether it was pressed this tick.
 */
@Getter
public class TrackedKeyBinding {
    private static final Map<KeyBinding, TrackedKeyBinding> bindings = new HashMap<>();
    private static boolean resetForScreen = false;
    private final KeyBinding parent;
    private boolean changedThisTick, pressedThisTick, releasedThisTick;

    static {
        // Reset values
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            bindings.values().forEach(TrackedKeyBinding::reset);
            if (client.currentScreen != null) {
                bindings.values().stream()
                        .filter(TrackedKeyBinding::isDown)
                        .forEach(TrackedKeyBinding::markReleased);
                resetForScreen = true;
            } else resetForScreen = false;
        });
    }

    private TrackedKeyBinding(KeyBinding parent) {
        this.parent = parent;
    }

    public static TrackedKeyBinding createAndRegister(String translationKey, InputUtil.Type type, int code, String category) {
        return wrap(KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, type, code, category)));
    }

    public static TrackedKeyBinding wrap(KeyBinding binding) {
        TrackedKeyBinding trackingBinding = new TrackedKeyBinding(binding);
        bindings.put(binding, trackingBinding);
        return trackingBinding;
    }

    public static void onKeyPressSet(KeyBinding binding, boolean pressed) {
        TrackedKeyBinding trackedBinding = bindings.get(binding);
        if (trackedBinding == null) return;

        if (pressed) trackedBinding.markPressed();
        else trackedBinding.markReleased();
    }

    public boolean isDown() {
        return parent.isPressed();
    }

    private void markPressed() {
        changedThisTick = true;
        if (!releasedThisTick) pressedThisTick = true;
    }

    private void markReleased() {
        // Released takes precedence.
        changedThisTick = true;
        pressedThisTick = false;
        releasedThisTick = true;
    }

    private void reset() {
        changedThisTick = pressedThisTick = releasedThisTick = false;
    }
}
