package net.arna.jcraft.common;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings("CanBeFinal") // They literally cannot
public class JConfig extends MidnightConfig {
    public enum UIPos {
        LEFT,
        RIGHT,
        MIDDLE
    }

    @Entry
    public static UIPos UI_POSITION = UIPos.RIGHT;

    @Entry
    public static boolean ICON_HUD = true;

    @Entry
    public static boolean TE_SHADER = true;

    @Entry
    public static boolean EPITAPH_OVERLAY = false;
}
