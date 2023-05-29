package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public class ShaderActivationPacket {
    public static final Identifier ID = new Identifier(JCraft.MOD_ID, "shader_packet");

    /**
     * Send a packet S2C to start rendering a shader of a specific {@link Type}
     *
     * @param serverPlayerEntity player who will se the shader
     * @param sourceShader       origin of the shader
     * @param tickDelay          delay before starting to render shader
     * @param duration           duration of the shader
     * @param type               which shader to use
     */
    public static void send(ServerPlayerEntity serverPlayerEntity, @Nullable LivingEntity sourceShader, int tickDelay, int duration, Type type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(tickDelay);
        buf.writeInt(duration);
        buf.writeString(type.asString());
        if (sourceShader != null) {
            buf.writeInt(sourceShader.getId());
        }
        ServerPlayNetworking.send(serverPlayerEntity, ID, buf);
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
            return this == ZA_WARUDO ? ZA_WARUDO.name : this == CRIMSON ? CRIMSON.name : NONE.name;
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
