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

    public static void send(ServerPlayerEntity serverPlayerEntity, LivingEntity sourceShader, int tickDelay, int duration, Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(sourceShader.getId());
        buf.writeInt(tickDelay);
        buf.writeInt(duration);
        buf.writeString(type.asString());
        ServerPlayNetworking.send(serverPlayerEntity, ID, buf);
    }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        ZaWarudoShaderHandler zaWarudoShaderHandler = JCraftClient.zaWarudoShader;
        int id = buf.readInt();
        int delay = buf.readInt();
        int duration = buf.readInt();
        Type type = Type.byName(buf.readString(), Type.NONE);
        client.execute(() -> {
            World world = client.world;
            if (world != null) {

                switch (type) {
                    case NONE -> {}
                    case ZA_WARDO -> {
                        Entity sourceShader = world.getEntityById(id);
                        if (sourceShader instanceof LivingEntity livingEntity) {
                            zaWarudoShaderHandler.tickDelay = delay;
                            zaWarudoShaderHandler.shaderSourceEntity = Optional.of(livingEntity).orElse(client.player);
                            zaWarudoShaderHandler.effectLength = duration;
                            zaWarudoShaderHandler.shouldRender = true;
                        }
                    }
                }

            }
        });
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
