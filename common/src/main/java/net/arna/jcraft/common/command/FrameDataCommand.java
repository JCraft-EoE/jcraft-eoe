package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.arna.jcraft.common.tickable.FrameDataRequests;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FrameDataCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("framedata")
                .then(Commands.literal("stand")
                        .requires(JPerms.FRAMEDATA_STAND.require())
                        .executes(
                                context -> run(context.getSource(), true)
                        )
                )
                .then(Commands.literal("spec")
                        .requires(JPerms.FRAMEDATA_SPEC.require())
                        .executes(
                                context -> run(context.getSource(), false)
                        )
                )
        );
    }

    public static int run(final CommandSourceStack source, final boolean stand) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        FrameDataRequests.add(player, stand ? FrameDataRequests.FrameDataType.STAND : FrameDataRequests.FrameDataType.SPEC);
        player.displayClientMessage(
                Component.translatable("jcraft.await.move"),
                false
        );
        return 1;
    }
}
