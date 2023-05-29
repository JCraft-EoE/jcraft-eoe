package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * If the shader should be canceled after its activation prior to its natural end, use this packet
 */
public class ShaderDeactivationPacket {
    public static final Identifier ID = new Identifier(JCraft.MOD_ID, "shader_deact_packet");

    public static void send(ServerPlayerEntity serverPlayerEntity, ShaderActivationPacket.Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(type.asString());
        ServerPlayNetworking.send(serverPlayerEntity, ID, buf);
    }
}
