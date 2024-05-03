package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.screenhandler.MenuScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.util.registry.Registry;

public interface JScreenHandlerTypeRegistry {

    ExtendedScreenHandlerType<MenuScreenHandler> MENU_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(((syncId, inventory, buf) -> new MenuScreenHandler(syncId)));

    static void init() {
        Registry.register(Registry.SCREEN_HANDLER, JCraft.id("menush"), MENU_SCREEN_HANDLER);
    }

}
