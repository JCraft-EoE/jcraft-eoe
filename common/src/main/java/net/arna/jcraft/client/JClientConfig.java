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
    // private boolean clientsidePrediction = false;
    private int horizontalHudOffset = 0;
    private int verticalHudOffset = 0;
    private boolean iconHudPeekAllMoves = true;
    private boolean iconHud = true;
    private boolean standAuras = true;
    private boolean disableNarrator = true;
    private boolean timeEraseShader = true;
    private boolean epitaphOverlay = true;
    private int dummyDamageIndicatorColor = 0xffff4444; // bright red
    private int dummyDamageIndicatorColorShadow = 0xff401010; // dark red shadow
    private boolean comboCounter = true;
    private boolean dynamicFirstPersonStandOpacity = true;
    private float firstPersonStandOpacityMult = 1.0f;

    public static void load() {
        instance = AutoConfig.getConfigHolder(JClientConfig.class).getConfig();
    }

    /*
    public void setClientsidePrediction(boolean clientsidePrediction) {
        this.clientsidePrediction = clientsidePrediction;
        NetworkManager.sendToServer(JPacketRegistry.C2S_PREDICTION_TRIGGER, PredictionTriggerPacket.write(clientsidePrediction));
    }
     */

    public enum UIPos {
        LEFT,
        RIGHT,
        MIDDLE
    }
}
