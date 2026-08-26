package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.api.spec.SpecData;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import static net.arna.jcraft.common.command.AboutStandCommand.appendMove;

@SuppressWarnings("removal")
public class AboutSpecCommand {
    private static final Component newLine = Component.literal("\n");

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spec")
                .then(Commands.literal("about")
                        .requires(JPerms.SPEC_ABOUT.require())
                        .executes(AboutSpecCommand::run)));
    }

    public static int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec == null) {
            player.displayClientMessage(Component.translatable("jcraft.commands.error.nospec"), false);
            return 0;
        }

        SpecType specType = spec.getType();
        SpecData data = spec.getSpecData();
        MutableComponent text = Component.empty();

        // Name
        text.append(Component.empty()
                .append(Component.literal("Name: "))
                .append(data.getName().copy().withStyle(ChatFormatting.YELLOW))
                .append(newLine));

        // Description
        text.append(data.getDescription().copy().withStyle(ChatFormatting.GREEN));
        text.append(Component.empty().append(newLine).append(newLine));

        // Attacks
        MutableComponent moves = Component.empty()
                .append(Component.literal("MOVES:").withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal("\n"));

        MoveMap<?, ?> moveMap = spec.getMoveMap();
        for (MoveClass type : MoveClass.values()) {
            for (MoveMap.Entry<?, ?> entry : moveMap.getEntries(type)) {
                // Move itself
                appendMove(entry, moves, Component.literal("● ").withStyle(ChatFormatting.GREEN), false);

                // Crouching variant
                if (entry.getCrouchingVariant() != null) {
                    appendMove(entry.getCrouchingVariant(), moves, Component.literal("  ● CROUCHING ").withStyle(ChatFormatting.DARK_AQUA), true);
                }

                // Aerial variant
                if (entry.getAerialVariant() != null) {
                    appendMove(entry.getAerialVariant(), moves, Component.literal("  ● AERIAL ").withStyle(ChatFormatting.GOLD), true);
                }
            }
        }
        text.append(moves);

        // Details
        text.append(Component.literal("\n"));
        text.append(data.getDetails());

        player.sendSystemMessage(text);
        return 1;
    }
}
