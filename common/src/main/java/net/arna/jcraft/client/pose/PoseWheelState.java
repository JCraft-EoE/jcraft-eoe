package net.arna.jcraft.client.pose;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.client.gui.screen.PoseWheelScreen;
import net.arna.jcraft.common.network.c2s.PoseTriggerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * Shared state that bridges {@code PoseWheelScreen} (input) and {@code PoseWheelOverlay}
 * (visuals): open flag, hovered slot index, currently active pose, and the windup → idle
 * countdown timer. Neither side owns the other; both read/write through this.
 */
public class PoseWheelState {

    @Getter
    private static boolean open = false;

    @Setter @Getter
    private static int hoveredSlot = -1;

    @Nullable private static String activePoseAnimId = null;
    @Getter
    private static boolean inIdle = false;
    private static int windupTicksLeft = 0;

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (open || mc.screen instanceof PoseWheelScreen) {
            mc.setScreen(null);
        } else {
            hoveredSlot = -1;
            mc.setScreen(new PoseWheelScreen());
        }
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PoseWheelScreen) mc.setScreen(null);
        open = false;
        hoveredSlot = -1;
    }

    public static void silentClose() {
        open = false;
        hoveredSlot = -1;
    }

    /** Synced from PoseWheelScreen.init/removed. */
    public static void markOpen(boolean isOpen) {
        open = isOpen;
        if (!isOpen) hoveredSlot = -1;
    }

    public static boolean hasPose() { return activePoseAnimId != null; }
    @Nullable public static String getActivePoseAnimId() { return activePoseAnimId; }

    public static void tick() {
        if (activePoseAnimId == null || inIdle) return;

        if (windupTicksLeft > 0) windupTicksLeft--;

        if (windupTicksLeft <= 0) {
            PoseDefinition def = findPose(activePoseAnimId);
            if (def != null && def.hasIdle()) {
                inIdle = true;
                NetworkManager.sendToServer(JPacketRegistry.C2S_POSE_TRIGGER,
                        PoseTriggerPacket.write(def.idleAnimId(), true));
            } else {
                activePoseAnimId = null;
            }
        }
    }

    /** Pressing the same active slot cancels. */
    public static void executeSlot(int index) {
        PoseDefinition def = PoseWheelConfig.getPoseAt(index);
        if (def == null) return;

        if (def.windupAnimId().equals(activePoseAnimId)) {
            cancelPose();
            return;
        }

        activePoseAnimId = def.windupAnimId();
        inIdle = false;
        windupTicksLeft = def.windupTicks();
        NetworkManager.sendToServer(JPacketRegistry.C2S_POSE_TRIGGER,
                PoseTriggerPacket.write(def.windupAnimId(), false));
    }

    public static void cancelPose() {
        if (activePoseAnimId == null) return;
        activePoseAnimId = null;
        inIdle = false;
        windupTicksLeft = 0;
        NetworkManager.sendToServer(JPacketRegistry.C2S_POSE_CANCEL,
                new FriendlyByteBuf(Unpooled.buffer()));
    }

    /** Server force-cancelled us (took damage, etc.). */
    public static void onServerCancel() {
        activePoseAnimId = null;
        inIdle = false;
        windupTicksLeft = 0;
    }

    @Nullable
    private static PoseDefinition findPose(String windupAnimId) {
        for (PoseDefinition def : PoseLoader.getPoses()) {
            if (def.windupAnimId().equals(windupAnimId)) return def;
        }
        return null;
    }
}
