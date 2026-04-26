package net.arna.jcraft.common.attack.moves.weatherreport;

public enum WindSpeed {
    LIGHT_BREEZE(0.06, "Light Breeze"),
    GALE(0.18, "Gale"),
    HURRICANE(0.36, "Hurricane");

    private final double velocity;
    private final String name;

    WindSpeed(double velocity, String name) {
        this.velocity = velocity;
        this.name = name;
    }

    public double velocity() { return velocity; }
    public String displayName() { return name; }
}
