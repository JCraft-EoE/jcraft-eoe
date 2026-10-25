package net.arna.jcraft.client.aim;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import lombok.Getter;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.network.c2s.GunAimPacket;
import net.arna.jcraft.common.system.GunAiming;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class GunAimHandler {
    public static final float ZOOM = 1.5f;
    public static final boolean HIDE_CROSSHAIR = true;

    private static final float PROGRESS_PER_TICK = 0.2f;
    private static boolean registered;
    @Getter
    private static boolean aiming;
    private static float progress;
    private static float lastProgress;

    private GunAimHandler() {}

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvent.CLIENT_POST.register(GunAimHandler::tick);
    }

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            aiming = false;
            progress = 0f;
            lastProgress = 0f;
            return;
        }
        boolean shouldAim = minecraft.screen == null
                && !player.isSpectator()
                && GunAiming.isAimableGun(player.getMainHandItem())
                && minecraft.options.keyUse.isDown();
        if (shouldAim != aiming) {
            aiming = shouldAim;
            NetworkManager.sendToServer(JPacketRegistry.C2S_GUN_AIM, GunAimPacket.write(aiming));
        }
        lastProgress = progress;
        progress = Mth.clamp(progress + (aiming ? PROGRESS_PER_TICK : -PROGRESS_PER_TICK), 0f, 1f);
    }

    public static float progress(float partialTick) {
        return Mth.lerp(partialTick, lastProgress, progress);
    }
}
