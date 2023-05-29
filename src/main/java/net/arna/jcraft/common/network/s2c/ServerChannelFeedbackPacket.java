package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerChannelFeedbackPacket {
    public static final Identifier ID = new Identifier(JCraft.MOD_ID, "sfchannel");

    public static void send(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        ServerPlayNetworking.send(serverPlayerEntity, ID, buf);
    }
}
