package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerAnimPacket {
    public static final Identifier ID = JCraft.id("animpacket");

    /**
     * Animates player (from) on player (to)'s end, while updating spec values
     * @param from ServerPlayerEntity to animate
     * @param to ServerPlayerEntity that views animation
     */
    public static void sendSpec(ServerPlayerEntity from, ServerPlayerEntity to, String animID, int moveStun, int attackID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(from.getId());
        buf.writeString(animID);

        buf.writeBoolean(true);
        buf.writeInt(moveStun);
        buf.writeInt(attackID);

        ServerPlayNetworking.send(to, ID, buf);
    }

    /**
     * @param from ServerPlayerEntity to animate
     * @param to ServerPlayerEntity that views animation
     */
    public static void send(ServerPlayerEntity from, ServerPlayerEntity to, String animID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(from.getId());
        buf.writeString(animID);
        buf.writeBoolean(false);

        ServerPlayNetworking.send(to, ID, buf);
    }
}
