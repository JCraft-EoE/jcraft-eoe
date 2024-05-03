package net.arna.jcraft.client;

import lombok.Getter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.c2s.PredictionTriggerPacket;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Annotations to use here: https://shedaniel.gitbook.io/cloth-config/auto-config/annotations
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Config(name = JCraft.MOD_ID)
public class JClientConfig implements ConfigData {
    @Getter
    @ConfigEntry.Gui.Excluded
    private static JClientConfig instance;

    private UIPos uiPosition = UIPos.RIGHT;
    private boolean clientsidePrediction = false;
    private boolean iconHud = true;
    private boolean standAuras = true;
    private boolean timeEraseShader = true;
    private boolean epitaphOverlay = true;

    public static void load() {
        instance = AutoConfig.getConfigHolder(JClientConfig.class).getConfig();
    }

    public void setClientsidePrediction(boolean clientsidePrediction) {
        this.clientsidePrediction = clientsidePrediction;

        ClientPlayNetworking.send(
                JPacketRegistry.C2S_PREDICTION_TRIGGER,
                PredictionTriggerPacket.write(clientsidePrediction)
        );
    }

    public enum UIPos {
        LEFT,
        RIGHT,
        MIDDLE
    }
}
