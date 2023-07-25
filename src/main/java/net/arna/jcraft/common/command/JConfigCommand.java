package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class JConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("jconfig")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeBoolean(ctx.getSource().hasPermissionLevel(2)); // editable
                    buf.writeBoolean(true); // show
                    // No need to write any options. Client should already have all of them.

                    ServerPlayNetworking.send(player, JPacketRegistry.S2C_SERVER_CONFIG, buf);
                    return 1;
                }));
    }
}
