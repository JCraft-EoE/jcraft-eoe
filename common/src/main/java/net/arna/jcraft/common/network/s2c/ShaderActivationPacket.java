package net.arna.jcraft.common.network.s2c;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
    public static void send(ServerPlayer serverPlayerEntity, @Nullable Entity sourceShader, int tickDelay, int duration, Type type) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(tickDelay);
        buf.writeInt(duration);
        buf.writeUtf(type.getSerializedName());
        if (sourceShader != null) {
            buf.writeInt(sourceShader.getId());
        }
        NetworkManager.sendToPlayer(serverPlayerEntity, JPacketRegistry.S2C_SHADER_ACTIVATION, buf);
    }

    /**
     * Send a MANDOM_REWIND shader packet with the attacker's aura color.
     */
    public static void sendMandomRewind(ServerPlayer target, int duration, Vector3f color) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(0); // tickDelay
        buf.writeInt(duration);
        buf.writeUtf(Type.MANDOM_REWIND.getSerializedName());
        buf.writeFloat(color.x());
        buf.writeFloat(color.y());
        buf.writeFloat(color.z());
        NetworkManager.sendToPlayer(target, JPacketRegistry.S2C_SHADER_ACTIVATION, buf);
    }

    public enum Type implements StringRepresentable {
        NONE("none"),
        ZA_WARUDO("za_warudo"),
        CRIMSON("crimson"),
        MANDOM_REWIND("mandom_rewind");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
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
