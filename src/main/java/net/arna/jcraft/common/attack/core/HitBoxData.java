package net.arna.jcraft.common.attack.core;

public class HitBoxData {
    public double forwardOffset = 0.0;
    public double verticalOffset = 0.0;
    public final double hitboxSize;

    public HitBoxData(double size) {
        this.hitboxSize = size;
    }

    public HitBoxData(double fO, double vO, double size) {
        this.forwardOffset = fO;
        this.verticalOffset = vO;
        this.hitboxSize = size;
    }
}
