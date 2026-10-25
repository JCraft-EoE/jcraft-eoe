package net.arna.jcraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * A KeyBinding that can tell you whether it was pressed this tick.
 */
@Getter
public class TrackedKeyBinding implements IJKeyBinding {
    private static final Map<KeyMapping, TrackedKeyBinding> bindings = new HashMap<>();
    private static boolean resetForScreen = false;
    private final KeyMapping parent;
    private boolean changedThisTick, pressedThisTick, releasedThisTick;

    public static void resetValues(final boolean clientScreenAssigned) {
        bindings.values().forEach(TrackedKeyBinding::reset);
        if (clientScreenAssigned) {
            bindings.values().stream()
                    .filter(TrackedKeyBinding::isDown)
                    .forEach(TrackedKeyBinding::markReleased);
            resetForScreen = true;
        } else {
            resetForScreen = false;
        }
    }

    private TrackedKeyBinding(final KeyMapping parent) {
        this.parent = parent;
    }

    public static TrackedKeyBinding create(final String translationKey, final InputConstants.Type type, final int code,
                                           final String category) {
        return wrap(new KeyMapping(translationKey, type, code, category));
    }

    public static TrackedKeyBinding wrap(final KeyMapping binding) {
        final TrackedKeyBinding trackingBinding = new TrackedKeyBinding(binding);
        bindings.put(binding, trackingBinding);
        return trackingBinding;
    }

    public static void onKeyPressSet(final KeyMapping binding, final boolean pressed) {
        final TrackedKeyBinding trackedBinding = bindings.get(binding);
        if (trackedBinding == null) {
            return;
        }

        if (pressed) {
            trackedBinding.markPressed();
        } else {
            trackedBinding.markReleased();
        }
    }

    public boolean isDown() {
        return parent.isDown();
    }

    private void markPressed() {
        changedThisTick = true;
        if (!releasedThisTick) {
            pressedThisTick = true;
        }
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
