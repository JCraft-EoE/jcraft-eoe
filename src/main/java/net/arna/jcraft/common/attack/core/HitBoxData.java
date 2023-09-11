package net.arna.jcraft.common.attack.core;

public record HitBoxData(double forwardOffset, double verticalOffset, double size) {

    public HitBoxData(double size) {
        this(0, 0, size);
    }
}
