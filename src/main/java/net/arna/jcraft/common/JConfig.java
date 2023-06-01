package net.arna.jcraft.common;

import eu.midnightdust.lib.config.MidnightConfig;

public class JConfig extends MidnightConfig {
    public enum UIPos {
        LEFT,
        RIGHT,
        MIDDLE
    }
    @Entry
    public static boolean SHADER_CRIMSON_SKY = false;
    @Entry
    public static boolean FORCE_CRIMSON_SKIES = false;
    @Entry
    public static boolean ANIME_VOICES = true;
    @Entry
    public static UIPos UI_POSITION = UIPos.RIGHT;
}
