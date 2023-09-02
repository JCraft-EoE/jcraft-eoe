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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class StandBlockPacket {
    private static final Set<ServerPlayerEntity> blocking = Collections.newSetFromMap(new WeakHashMap<>());

    public static PacketByteBuf write(boolean isBlocking) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isBlocking);
        return buf;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        boolean blockDown = buf.readBoolean();
        server.execute(() -> {
            if (blockDown) blocking.add(player);
            else blocking.remove(player);

            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (!JCraft.isDashing(player) && stand != null) {
                boolean blocking = stand.blocking;
                if (!blocking && blockDown) {
                    if (player.getMainHandStack().getUseAction() == UseAction.NONE && player.getOffHandStack().getUseAction() == UseAction.NONE) {
                        stand.wantToBlock = true;
                        if (stand.canAttack()) stand.blocking = true;
                    }
                } else if (blocking && !blockDown) stand.wantToBlock = false;
            }
        });
    }

    public static boolean isBlocking(ServerPlayerEntity player) {
        return blocking.contains(player);
    }
}
