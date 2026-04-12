package net.arna.jcraft.api.component.living;


public interface CommonGravityShiftComponent {
    void startRadial();

    void startDirectional(final int range);

    void swapRadialType();

    boolean isActive();

    void stop();
}
