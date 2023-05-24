package net.arna.jcraft.client.network.s2c;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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

    /**
     * This is the client handling your packet with shader info in
     *
     * @param client                   mc
     * @param clientPlayNetworkHandler .
     * @param buf                      packet
     * @param packetSender             .
     */
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        ShaderActivationPacket.Type type = ShaderActivationPacket.Type.byName(buf.readString());
        World world = client.world;
        if (world != null) {
            switch (type) {
                case NONE -> {
                }
                case ZA_WARUDO -> {
                    client.execute(() -> {
                        ZaWarudoShaderHandler zaWarudoShaderHandler = ZaWarudoShaderHandler.INSTANCE;
                        zaWarudoShaderHandler.shouldRender = false;
                        zaWarudoShaderHandler.renderingEffect = false;
                    });
                }
            }
        }
    }
}
