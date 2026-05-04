package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class JCraftHelpCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jcraft")
                .then(Commands.literal("help")
                        .executes(JCraftHelpCommand::run)
                )
        );
    }

    private static final Style trelloStyle = Style.EMPTY
            .withColor(ChatFormatting.BLUE)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://trello.com/b/B5Q7ZthB/jcraft-eyes-of-ender-community-trello"));

    public static int run(final CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() ->
                Component.literal("""
                        §c/stand about§r
                        Displays all relevant Stand information, if your stand is SUMMONED
                        §c/stand set§r
                        Sets your stand (and optionally skin)
                        §c/stand clear§r
                        Clears your stand (also see /stand set ? NONE
                        §c/stand skin§r
                        Sets your stand's skin (0-3)
                        §c/stand block§r
                        Makes the target block or unblock with their stand
                        §c/spec about§r
                        Displays all relevant Spec information
                        §c/spec clear§r
                        Clears your current spec
                        §c/spec reset§r
                        Resets spec data
                        §c/spec set§r
                        Sets your spec
                        §c/spec unlock§r
                        Fully progresses your current spec (only if it has a progression system)
                        §c/attack§r
                        Causes targets to start a spec/stand attack, which IGNORES COOLDOWNS
                        §c/framedata§r
                        Displays information about moves your stand or spec did after starting the command
                        §c/jgravity§r
                        Sets your gravity direction
                        §c/jwiki§r
                        Sends a link to the OFFICIAL Jcraft:EoE Wiki
                        """), false
        );
        // https://trello.com/b/B5Q7ZthB/jcraft-eyes-of-ender-community-trello
        ctx.getSource().sendSuccess(() -> Component.translatable("jcraft.trello.link")
                .withStyle(trelloStyle), false);
        return 1;
    }
}
