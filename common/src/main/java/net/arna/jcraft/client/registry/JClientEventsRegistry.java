package net.arna.jcraft.client.registry;

import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.registry.menu.MenuRegistry;
import net.arna.jcraft.client.events.JClientEvents;
import net.arna.jcraft.client.gui.screen.DiscCaseMenuScreen;
import net.arna.jcraft.client.gui.screen.MainMenuScreen;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.api.registry.JMenuRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface JClientEventsRegistry {
    static void registerClientEvents() {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(JClientEvents::clientPlayerJoin);

        // This HAS to be registered before TrackingKeyBinding is initialized.
        ClientTickEvent.CLIENT_POST.register(JClientEvents::tickClient);
        ClientTickEvent.CLIENT_LEVEL_POST.register(level -> new SkyBoxManager());

        ClientGuiEvent.RENDER_HUD.register(JClientEvents::renderHud);
        EntityEvent.ADD.register(JClientEvents::onEntityAdd);

        MenuRegistry.registerScreenFactory(JMenuRegistry.MAIN_MENU_TYPE.get(), MainMenuScreen::new);
        MenuRegistry.registerScreenFactory(JMenuRegistry.DISC_CASE_MENU_TYPE.get(), DiscCaseMenuScreen::new);

        ClientCommandRegistrationEvent.EVENT.register((dispatcher, ctx) -> JClientCommandRegistry.registerCommands(dispatcher));
    }
}
