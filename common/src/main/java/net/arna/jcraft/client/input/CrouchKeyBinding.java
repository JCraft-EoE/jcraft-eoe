package net.arna.jcraft.client.input;

import lombok.Getter;
import net.minecraft.client.Minecraft;

// This binding is used to determine when a player is crouching for doing crouching variants of moves.
// When the player is riding something, we consider sprinting to be the equivalent of crouching.
public class CrouchKeyBinding implements IJKeyBinding{
    public static final CrouchKeyBinding INSTANCE = new CrouchKeyBinding();
    @Getter(lazy = true)
    private final TrackedKeyBinding vanillaCrouch = TrackedKeyBinding.wrap(Minecraft.getInstance().options.keyShift),
            vanillaSprint = TrackedKeyBinding.wrap(Minecraft.getInstance().options.keySprint);

    private CrouchKeyBinding() {}

    @Override
    public boolean isChangedThisTick() {
        return (isPlayerRiding() ? getVanillaSprint() : getVanillaCrouch()).isChangedThisTick();
    }

    @Override
    public boolean isPressedThisTick() {
        return (isPlayerRiding() ? getVanillaSprint() : getVanillaCrouch()).isPressedThisTick();
    }

    @Override
    public boolean isReleasedThisTick() {
        return (isPlayerRiding() ? getVanillaSprint() : getVanillaCrouch()).isReleasedThisTick();
    }

    @Override
    public boolean isDown() {
        return (isPlayerRiding() ? getVanillaSprint() : getVanillaCrouch()).isDown();
    }

    private boolean isPlayerRiding() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        return mc.player.getVehicle() != null;
    }
}
