package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UseAction;

public class StandBlockPacket {

    public static PacketByteBuf write(boolean isBlocking) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isBlocking);
        return buf;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        boolean rmb = buf.readBoolean();
        server.execute(() -> {
            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (!JCraft.isDashing(player) && stand != null) {
                boolean blocking = stand.blocking;
                if (!blocking && stand.canAttack() && rmb) {
                    if (player.getMainHandStack().getUseAction() == UseAction.NONE &&
                            player.getOffHandStack().getUseAction() == UseAction.NONE)
                        stand.blocking = true;
                } else if (blocking && !rmb) stand.blocking = false;
            }
        });
    }
}
