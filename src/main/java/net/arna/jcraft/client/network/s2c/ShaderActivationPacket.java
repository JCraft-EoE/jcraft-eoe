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
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ShaderActivationPacket {
    public static final Identifier ID = new Identifier(JCraft.MOD_ID, "shader_packet");

    /**
     * Send a packet S2C to start rendering a shader of a specific {@link Type}
     * @param serverPlayerEntity player who will se the shader
     * @param sourceShader origin of the shader, if not null, must call buf.readInt() in {@link #handle(MinecraftClient, ClientPlayNetworkHandler, PacketByteBuf, PacketSender)} switch
     * @param tickDelay delay before starting to render shader
     * @param duration duration of the shader
     * @param type which shader to use
     */
    public static void send(ServerPlayerEntity serverPlayerEntity, @Nullable LivingEntity sourceShader, int tickDelay, int duration, Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(tickDelay);
        buf.writeInt(duration);
        buf.writeString(type.asString());
        if(sourceShader != null){
            buf.writeInt(sourceShader.getId());
        }
        ServerPlayNetworking.send(serverPlayerEntity, ID, buf);
    }

    /**
     * This is the client handling your packet with shader info in
     * @param client mc
     * @param clientPlayNetworkHandler .
     * @param buf packet
     * @param packetSender .
     */
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        ZaWarudoShaderHandler zaWarudoShaderHandler = JCraftClient.zaWarudoShader;
        int delay = buf.readInt();
        int duration = buf.readInt();
        Type type = Type.byName(buf.readString());

        World world = client.world;
        if (world != null) {
            switch (type) {
                case NONE -> { }
                case ZA_WARDO -> {
                    int id = buf.readInt();
                    client.execute(() -> {
                        Entity sourceShader = world.getEntityById(id);
                        if (sourceShader instanceof LivingEntity livingEntity) {
                            zaWarudoShaderHandler.tickDelay = delay;
                            zaWarudoShaderHandler.shaderSourceEntity = Optional.of(livingEntity).orElse(client.player);
                            zaWarudoShaderHandler.effectLength = duration;
                            zaWarudoShaderHandler.shouldRender = true;
                        }
                    });
                }
            }
        }
    }

    public enum Type implements StringIdentifiable {
        NONE("none"),
        ZA_WARDO("za_warudo");

        private final String name;

        Type(String name){
            this.name = name;
        }

        @Override
        public String asString() {
            return this == ZA_WARDO ? ZA_WARDO.name : NONE.name;
        }

        public String getName() {
            return name;
        }

        public static Type byName(String name){
            return byName(name, NONE);
        }

        public static Type byName(String name, @Nullable Type defaultType) {
            Type[] var2 = values();
            for (Type type : var2) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return defaultType;
        }
    }
}
