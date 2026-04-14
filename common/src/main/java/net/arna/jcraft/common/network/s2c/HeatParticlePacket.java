package net.arna.jcraft.common.network.s2c;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class HeatParticlePacket {
    public static void send(ServerPlayer target, Vec3 pos, float bbHeight, float spread, int heat) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeFloat(bbHeight);
        buf.writeFloat(spread);
        buf.writeInt(heat);

        NetworkManager.sendToPlayer(target, JPacketRegistry.S2C_HEAT_PARTICLE, buf);
    }
}
