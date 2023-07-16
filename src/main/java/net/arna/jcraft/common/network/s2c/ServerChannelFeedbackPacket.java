package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerChannelFeedbackPacket {
    public static void send(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        ServerPlayNetworking.send(serverPlayerEntity, JPacketRegistry.S2C_SERVER_CHANNEL_FEEDBACK, buf);
    }
}
