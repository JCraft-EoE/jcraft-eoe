package net.arna.jcraft.client.events;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

public class JJoinServerEvents implements ClientPlayConnectionEvents.Join {

    @Override
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        ClientPlayerEntity clientPlayer = client.player;
        if (clientPlayer == null) {
            JCraft.LOGGER.fatal("onPlayReady was called with invalid client player!");
            return;
        }

        // used to send an OnConnectedPacket
    }
}
