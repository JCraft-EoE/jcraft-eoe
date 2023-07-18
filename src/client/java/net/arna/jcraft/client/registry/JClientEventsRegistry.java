package net.arna.jcraft.client.registry;

import net.arna.jcraft.client.events.JJoinServerEvents;
import net.arna.jcraft.client.events.JWorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public interface JClientEventsRegistry {
    static void registerClientEvents() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(JWorldRenderEvents::afterTranslucent);

        WorldRenderEvents.LAST.register(JWorldRenderEvents::onLast);

        ClientPlayConnectionEvents.JOIN.register(new JJoinServerEvents());
    }
}
