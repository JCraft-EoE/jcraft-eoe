package net.arna.jcraft.client.input;

import dev.architectury.event.events.client.ClientTickEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;

// This binding is used to determine when a player is in the air for doing aerial variants of moves.
// When the player is riding something, we consider holding space to be the equivalent of jumping.
public class AerialKeyBinding implements IJKeyBinding {
    public static final AerialKeyBinding INSTANCE = new AerialKeyBinding();
    @Getter(lazy = true)
    private final TrackedKeyBinding vanillaJump = TrackedKeyBinding.wrap(Minecraft.getInstance().options.keyJump);
    private boolean wasSpaceHeld = false, wasAirborne = false, updatedThisTick = false;

    static {
        ClientTickEvent.CLIENT_LEVEL_PRE.register(l -> INSTANCE.reset());
    }

    private AerialKeyBinding() {}

    @Override
    public boolean isChangedThisTick() {
        if (!isPlayerRiding()) {
            boolean isAirborne = isPlayerAirborne();
            boolean changed = wasAirborne != isAirborne;

            if (!updatedThisTick) {
                updatedThisTick = true;
                wasAirborne = isAirborne;
            }

            return changed;
        }

        boolean isSpaceHeld = isSpaceHeld();
        boolean changed = isSpaceHeld != wasSpaceHeld;

        if (!updatedThisTick) {
            updatedThisTick = true;
            wasSpaceHeld = isSpaceHeld;
        }

        return changed;
    }

    @Override
    public boolean isPressedThisTick() {
        if (!isPlayerRiding()) return isPlayerAirborne() && !wasAirborne;

        return isChangedThisTick() && isSpaceHeld();
    }

    @Override
    public boolean isReleasedThisTick() {
        if (!isPlayerRiding()) return !isPlayerAirborne() && wasAirborne;

        return isChangedThisTick() && !isSpaceHeld();
    }

    @Override
    public boolean isDown() {
        if (!isPlayerRiding()) return isPlayerAirborne();

        return isSpaceHeld();
    }

    private boolean isPlayerRiding() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        return mc.player.getVehicle() != null;
    }

    private boolean isSpaceHeld() {
        return getVanillaJump().isDown();
    }

    private boolean isPlayerAirborne() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        return !mc.player.onGround();
    }

    private void reset() {
        updatedThisTick = false;
    }
}
