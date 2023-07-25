package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Set;

public class ConfigUpdatePacket {
    public static final Identifier ID = JCraft.id("config_update");

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        // Only operators can send this packet
        if (server.getPermissionLevel(player.getGameProfile()) < 2) {
            JCraft.LOGGER.warn("Player {} send a config update packet while not having permission to do so.",
                    player.getGameProfile().getName());
            player.networkHandler.disconnect(Text.literal("You do not have permission to update the JCraft server config."));
            return;
        }

        JServerConfig.save(server);

        Set<ConfigOption> changedOptions = ConfigOption.readOptions(buf);

        // Broadcast changes to everyone except the person who made them.
        PacketByteBuf clientChangesBuf = writeClientChanges(changedOptions);
        for (ServerPlayerEntity serverPlayer : PlayerLookup.all(server))
            if (serverPlayer != player)
                ServerPlayNetworking.send(serverPlayer, JPacketRegistry.S2C_SERVER_CONFIG, clientChangesBuf);
    }

    private static PacketByteBuf writeClientChanges(Collection<ConfigOption> options) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(false); // Editable, doesn't matter if show is false
        buf.writeBoolean(false); // Show

        ConfigOption.writeOptions(buf, options);
        return buf;
    }

    public static void sendOptionsToClient(ServerPlayerEntity player, Collection<ConfigOption> options) {
        ServerPlayNetworking.send(player, JPacketRegistry.S2C_SERVER_CONFIG, writeClientChanges(options));
    }
}
