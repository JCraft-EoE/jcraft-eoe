package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

//todo: replace with a Patchouli in-game wikipedia
@Deprecated(forRemoval = true)
public class AboutStandCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("about").executes(AboutStandCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        StandEntity<?, ?> stand = JUtils.getStand(player);

        if (stand == null) {
            context.getSource().sendFeedback(() -> Text.translatable("jcraft.commands.error.nostand"), true);
            return 0;
        }

        MutableText resp = Text.empty();

        // Name
        resp.append(Text.empty()
                .append(Text.literal("Name: "))
                .append(stand.getName().copy().formatted(Formatting.YELLOW))
                .append(Text.literal("\n")));

        // Description
        resp.append(Text.literal(stand.description).formatted(Formatting.GREEN))
                .append(Text.literal("\n"));

        // Pros & Cons
        MutableText pros = Text.empty()
                .append(Text.literal("PROS:").formatted(Formatting.DARK_AQUA))
                .append(Text.literal("\n"));
        for (String pro : stand.pros) pros
                .append(Text.literal("● ").formatted(Formatting.DARK_AQUA))
                .append(Text.literal(pro))
                .append(Text.literal("\n"));
        resp.append(pros);

        MutableText cons = Text.empty()
                .append(Text.literal("CONS:").formatted(Formatting.DARK_RED))
                .append(Text.literal("\n"));
        for (String con : stand.cons) cons
                .append(Text.literal("● ").formatted(Formatting.DARK_RED))
                .append(Text.literal(con))
                .append(Text.literal("\n"));
        resp.append(cons);

        resp.append(Text.literal("\n"));

        // Moves
        MutableText moves = Text.empty()
                .append(Text.literal("MOVES:").formatted(Formatting.DARK_GREEN))
                .append(Text.literal("\n"));

        MoveMap<?, ?> moveMap = stand.getMoveMap();
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
        resp.append(moves);

        // Free Space
        resp.append(Text.literal("\n"));
        resp.append(Text.literal(stand.freespace));

        context.getSource().sendFeedback(() -> resp, false);
        return 1;
    }

    public static void appendMove(MoveMap.Entry<?, ?> entry, MutableText moves, MutableText base, boolean isVariant) {
        moves
                .append(base
                        .append(isVariant ? Text.empty() : Text.empty()
                                .append(entry.getType().getFriendlyName())
                                .append(Text.empty()
                                        .append(Text.literal(" ("))
                                        .append(entry.getType().getKey().copy().formatted(Formatting.AQUA))
                                        .append(Text.literal(")")))))
                .append(Text.literal(" - "))
                .append(entry.getMove().getName().copy().formatted(Formatting.DARK_PURPLE))
                .append(Text.literal(" - "))
                .append(entry.getMove().getDescription())
                .append(Text.literal("\n"));
    }
}
