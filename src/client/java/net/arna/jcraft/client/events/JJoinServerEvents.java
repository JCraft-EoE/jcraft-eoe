package net.arna.jcraft.client.events;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.network.c2s.OnConnectedPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class JJoinServerEvents implements ClientPlayConnectionEvents.Join {

    @Override
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        ClientPlayerEntity clientPlayer = client.player;
        if (clientPlayer == null) {
            JCraft.LOGGER.fatal("onPlayReady was called with invalid client player!");
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(clientPlayer.getModel().equals("slim"));

        sender.sendPacket(OnConnectedPacket.ID, buf);
    }
}
