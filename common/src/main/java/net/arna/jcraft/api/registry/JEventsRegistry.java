package net.arna.jcraft.api.registry;

import dev.architectury.event.events.common.*;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.events.JBlockEvents;
import net.arna.jcraft.common.events.JEntityEvents;
import net.arna.jcraft.common.events.JServerEvents;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.arna.jcraft.common.network.s2c.AttackerDataPacket;

import java.util.List;

public interface JEventsRegistry {
    static void registerEvents() {
        // NOTE: THESE ARE ALSO REGISTERED ON THE CLIENT
        EntityEvent.LIVING_HURT.register(JServerEvents::hurt);
        JEntityEvents.POST_ADD.register(JServerEvents::entityLoad);
        EntityEvent.LIVING_DEATH.register(JServerEvents::death);
        TickEvent.SERVER_POST.register(JServerEvents::serverPostTick);
        TickEvent.SERVER_LEVEL_POST.register(JServerEvents::serverLevelPostTick);
        JBlockEvents.BEFORE_SET.register(JServerEvents::beforeBlockSet);
        JBlockEvents.BEFORE_BLOCK_LOOT.register(JServerEvents::processBlockLoot);

        // Disable item/block usage while stunned
        InteractionEvent.RIGHT_CLICK_ITEM.register(JServerEvents::rightClick);

        InteractionEvent.LEFT_CLICK_BLOCK.register(JServerEvents::leftClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(JServerEvents::rightClickBlock);

        // Send initial values of server config options to the player.
        PlayerEvent.PLAYER_JOIN.register((player) -> {
            ConfigUpdatePacket.sendOptionsToClient(player, ConfigOption.getImmutableOptions().values());
            AttackerDataPacket.send(List.of(player));
        });

        LifecycleEvent.SERVER_BEFORE_START.register(JServerConfig::load);
        LifecycleEvent.SERVER_STARTED.register(JServerEvents::finishLoading);
        LifecycleEvent.SERVER_STOPPED.register(JServerEvents::saveExclusives);
    }
}
