package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

        SpecType type = spec.getType();
        MutableText text = Text.empty();

        // Name
        text.append(Text.empty()
                .append(Text.literal("Name: "))
                .append(type.getTranslatableName().copy().formatted(Formatting.YELLOW))
                .append(newLine));

        // Description
        text.append(type.getDescription().copy().formatted(Formatting.GREEN));
        text.append(Text.empty().append(newLine).append(newLine));

        /*
        // Pros & Cons
        readout.append("§3PROS:§r\n");
        for (String s : stand.pros) {
            readout.append("§3●§r ").append(s).append("\n");
        }
        readout.append("§4CONS:§r\n");
        for (String s : stand.cons) {
            readout.append("§4●§r ").append(s).append("\n");
        }

        readout.append("\n");
         */

        // Attacks
        text.append(Text.literal("Attacks:").formatted(Formatting.GREEN));
        for (MoveMap.Entry<?, ?> entry : spec.getMoveMap()) {
            if (entry.getType() == null) continue; // Some variant of another attack.

            appendVariant(text, entry, Formatting.DARK_GREEN, Text.literal("● "));
            if (entry.getCrouchingVariant() != null)
                appendVariant(text, entry.getCrouchingVariant(), Formatting.DARK_AQUA, Text.literal("  ●CROUCHING "));
            if (entry.getAerialVariant() != null)
                appendVariant(text, entry.getAerialVariant(), Formatting.DARK_RED, Text.literal("  ●AERIAL "));
        }

        // Details
        text.append(type.getDetails());

        player.sendMessage(text);
        return 1;
    }

    private static void appendVariant(MutableText base, MoveMap.Entry<?, ?> entry, Formatting color, Text variantName) {
        base.append(Text.empty()
                .append(Text.empty().formatted(color)
                        .append(variantName)
                        .append(entry.getType() == null ? Text.empty() : entry.getType().getFriendlyName()))
                .append(entry.getMove().getName().copy().formatted(Formatting.DARK_PURPLE))
                .append(entry.getMove().getDescription().copy())
                .append(newLine));
    }
}
