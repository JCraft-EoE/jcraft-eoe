package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.arna.jcraft.common.command.AboutStandCommand.appendMove;

public class AboutSpecCommand {
    private static final Text newLine = Text.literal("\n");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spec")
                .then(CommandManager.literal("about")
                        .executes(AboutSpecCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec == null) {
            player.sendMessage(Text.translatable("jcraft.commands.error.nospec"), false);
            return 0;
        }

        SpecType specType = spec.getType();
        MutableText text = Text.empty();

        // Name
        text.append(Text.empty()
                .append(Text.literal("Name: "))
                .append(specType.getTranslatableName().copy().formatted(Formatting.YELLOW))
                .append(newLine));

        // Description
        text.append(specType.getDescription().copy().formatted(Formatting.GREEN));
        text.append(Text.empty().append(newLine).append(newLine));

        // Attacks
        MutableText moves = Text.empty()
                .append(Text.literal("MOVES:").formatted(Formatting.DARK_GREEN))
                .append(Text.literal("\n"));

        MoveMap<?, ?> moveMap = spec.getMoveMap();
        for (MoveType type : MoveType.values())
            for (MoveMap.Entry<?, ?> entry : moveMap.getEntries(type)) {
                // Move itself
                appendMove(entry, moves, Text.literal("● ").formatted(Formatting.GREEN), false);

                // Crouching variant
                if (entry.getCrouchingVariant() != null)
                    appendMove(entry.getCrouchingVariant(), moves, Text.literal("  ● CROUCHING ").formatted(Formatting.DARK_AQUA), true);

                // Aerial variant
                if (entry.getAerialVariant() != null)
                    appendMove(entry.getAerialVariant(), moves, Text.literal("  ● AERIAL ").formatted(Formatting.GOLD), true);
            }
        text.append(moves);

        // Details
        text.append(Text.literal("\n"));
        text.append(specType.getDetails());

        player.sendMessage(text);
        return 1;
    }
}
