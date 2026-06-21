package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.api.serverconfig.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConfigUpdatePacket {
    public static final ResourceLocation ID = JCraft.id("config_update");

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        MinecraftServer server = context.getPlayer().getServer();

        if (server == null) return; // should not be possible.

        // Only operators can send this packet
        if (!player.hasPermissions(2) && !server.isSingleplayer()) {
            JCraft.LOGGER.warn("Player {} sent a config update packet while not having permission to do so.",
                    player.getGameProfile().getName());
            player.connection.disconnect(Component.literal("You do not have permission to update the JCraft server config."));

            // Mark packet as read
            buf.readerIndex(buf.readableBytes());
            return;
        }

        // Apply changes and save.
        Set<ConfigOption> changedOptions = JServerConfig.readOptions(buf);
        JServerConfig.save(server);

        // Broadcast changes to everyone except the person who made them.
        final FriendlyByteBuf clientChangesBuf = writeClientChanges(changedOptions);
        final Set<ServerPlayer> receivers = new HashSet<>(server.getPlayerList().getPlayers());
        receivers.remove(player);
        NetworkManager.sendToPlayers(receivers, JPacketRegistry.S2C_SERVER_CONFIG, clientChangesBuf);
    }

    private static FriendlyByteBuf writeClientChanges(Collection<ConfigOption> options) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(false); // Editable, doesn't matter if show is false
        buf.writeBoolean(false); // Show

        JServerConfig.writeOptions(buf, options);
        return buf;
    }

    public static void sendOptionsToClient(ServerPlayer player, Collection<ConfigOption> options) {
        NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_SERVER_CONFIG, writeClientChanges(options));
    }
}
