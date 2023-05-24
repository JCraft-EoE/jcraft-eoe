package net.arna.jcraft.client.network.s2c;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.JCraftClient;
import net.arna.jcraft.client.rendering.handler.ZaWarudoShaderHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class ShaderActivationPacket {
    public static final Identifier ID = new Identifier(JCraft.MOD_ID, "shader_packet");

    public static void send(ServerPlayerEntity serverPlayerEntity, LivingEntity sourceShader, int tickDelay) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(sourceShader.getId());
        buf.writeInt(tickDelay);
        ServerPlayNetworking.send(serverPlayerEntity, ID, buf);
    }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        ZaWarudoShaderHandler zaWarudoShaderHandler = JCraftClient.zaWarudoShader;
        int id = buf.readInt();
        int delay = buf.readInt();
        client.execute(() -> {
            World world = client.world;
            if (world != null) {
                Entity sourceShader = world.getEntityById(id);
                if (sourceShader instanceof LivingEntity livingEntity) {
                    zaWarudoShaderHandler.tickDelay = delay;
                    zaWarudoShaderHandler.shaderSourceEntity = Optional.of(livingEntity).orElse(client.player);
                    zaWarudoShaderHandler.effectLength = 300;
                    zaWarudoShaderHandler.shouldRender = true;
                }
            }
        });
    }
}
