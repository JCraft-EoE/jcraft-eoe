package net.arna.jcraft.client;

import lombok.Getter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.arna.jcraft.JCraft;

// Annotations to use here: https://shedaniel.gitbook.io/cloth-config/auto-config/annotations
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Config(name = JCraft.MOD_ID)
public class JClientConfig implements ConfigData {
    @Getter
    @ConfigEntry.Gui.Excluded
    private static JClientConfig instance;

    private UIPos uiPosition = UIPos.RIGHT;
    private boolean iconHud = true;
    private boolean timeEraseShader = true;
    private boolean epitaphOverlay = true;

    public static void load() {
        instance = AutoConfig.getConfigHolder(JClientConfig.class).getConfig();
    }

    public enum UIPos {
        LEFT,
        RIGHT,
        MIDDLE
    }
}
