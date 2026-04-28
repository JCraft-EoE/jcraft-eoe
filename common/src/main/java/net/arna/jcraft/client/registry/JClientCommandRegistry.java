package net.arna.jcraft.client.registry;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import net.arna.jcraft.client.command.*;

public interface JClientCommandRegistry {

    static void registerCommands(CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher) {
        JPoseCommand.register(dispatcher);
        JWikiCommand.register(dispatcher);
    }
}
