package net.arna.jcraft.client.input;

public interface IJKeyBinding {
    boolean isChangedThisTick();

    boolean isPressedThisTick();

    boolean isReleasedThisTick();

    boolean isDown();
}
