package net.arna.jcraft.util;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.arna.jcraft.events.PlayerEvents;

public class ModEventsRegister {
    public static void registerEvents() {
        ServerPlayerEvents.COPY_FROM.register(new PlayerEvents());
    }
}