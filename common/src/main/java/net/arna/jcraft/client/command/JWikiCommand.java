package net.arna.jcraft.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class JWikiCommand {
    private static final String WIKI_URL = "https://wiki.jcraft-eoe.com/";

    public static void register(CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<ClientCommandRegistrationEvent.ClientCommandSourceStack>literal("jwiki")
                .executes(JWikiCommand::runWiki));
    }

    private static int runWiki(CommandContext<ClientCommandRegistrationEvent.ClientCommandSourceStack> ctx) {
        Component message = Component.literal("JCraft: Eyes of Ender wiki: ")
                .append(Component.literal(WIKI_URL)
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WIKI_URL))
                                .withUnderlined(true)));

        ctx.getSource().arch$sendSuccess(() -> message, false);
        return 1;
    }
}