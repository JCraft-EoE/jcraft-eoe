package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.events.JPlayerEntityEvents;
import net.arna.jcraft.common.events.JServerTickEvents;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public interface JEventsRegister {
    static void registerEvents() {
        ServerPlayerEvents.COPY_FROM.register(new JPlayerEntityEvents());
        ServerLivingEntityEvents.AFTER_DEATH.register(
                (living, source) -> {
                    if (living instanceof ServerPlayerEntity player) {
                        ((IEntityDataSaver) player).getPersistentData().putInt(JCraft.comboBreakerCD, 0);
                    }
                }
        );

        ServerTickEvents.END_SERVER_TICK.register(JServerTickEvents::serverTick);
    }
}
