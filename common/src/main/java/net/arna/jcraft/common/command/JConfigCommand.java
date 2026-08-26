package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class JConfigCommand {

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jconfig")
                .requires(JPerms.CONFIG.require())
                .executes(JConfigCommand::run));
    }

    private static int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack source = ctx.getSource();
        final ServerPlayer player = source.getPlayerOrException();

        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(JPerms.CONFIG_EDIT.sourceHas(source)); // editable
        buf.writeBoolean(true); // show
        // No need to write any options. Client should already have all of them.

        NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_SERVER_CONFIG, buf);
        return 1;
    }
}
