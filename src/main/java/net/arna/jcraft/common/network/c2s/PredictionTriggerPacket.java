package net.arna.jcraft.common.network.c2s;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class PredictionTriggerPacket {
    private static final Set<ServerPlayerEntity> subscribers = Collections.newSetFromMap(new WeakHashMap<>());

    public static PacketByteBuf write(boolean enable) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enable);
        return buf;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        boolean enable = buf.readBoolean();
        server.execute(() -> {
            if (enable) subscribers.add(player);
            else subscribers.remove(player);
        });
    }

    public static Set<ServerPlayerEntity> getSubscribers() {
        return subscribers;
    }

    public static boolean isSubscribed(ServerPlayerEntity player) {
        return subscribers.contains(player);
    }
}
