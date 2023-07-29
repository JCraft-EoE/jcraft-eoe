package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ComboCounterPacket {
    public static void send(ServerPlayerEntity player, int comboCount, float damageScaling) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(comboCount);
        buf.writeFloat(damageScaling);

        ServerPlayNetworking.send(player, JPacketRegistry.S2C_COMBO_COUNTER, buf);
    }
}
