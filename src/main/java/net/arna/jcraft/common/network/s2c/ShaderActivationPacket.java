package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public class ShaderActivationPacket {

    /**
     * Send a packet S2C to start rendering a shader of a specific {@link Type}
     *
     * @param serverPlayerEntity player who will se the shader
     * @param sourceShader       origin of the shader
     * @param tickDelay          delay before starting to render shader
     * @param duration           duration of the shader
     * @param type               which shader to use
     */
    public static void send(ServerPlayerEntity serverPlayerEntity, @Nullable Entity sourceShader, int tickDelay, int duration, Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(tickDelay);
        buf.writeInt(duration);
        buf.writeString(type.asString());
        if (sourceShader != null) {
            buf.writeInt(sourceShader.getId());
        }
        ServerPlayNetworking.send(serverPlayerEntity, JPacketRegistry.S2C_SHADER_ACTIVATION, buf);
    }

    public enum Type implements StringIdentifiable {
        NONE("none"),
        ZA_WARUDO("za_warudo"),
        CRIMSON("crimson");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public static Type byName(String name) {
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
