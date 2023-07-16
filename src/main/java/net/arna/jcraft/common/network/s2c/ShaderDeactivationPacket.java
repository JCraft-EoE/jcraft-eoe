package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * If the shader should be canceled after its activation prior to its natural end, use this packet
 */
public class ShaderDeactivationPacket {

    public static void send(ServerPlayerEntity serverPlayerEntity, ShaderActivationPacket.Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(type.asString());
        ServerPlayNetworking.send(serverPlayerEntity, JPacketRegistry.S2C_SHADER_DEACTIVATION, buf);
    }
}
