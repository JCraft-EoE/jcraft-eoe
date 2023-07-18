package net.arna.jcraft.common.events;

import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

// Make sure data that's meant to be persisted isn't wiped when the player dies
public class JPlayerEntityEvents implements ServerPlayerEvents.CopyFrom {
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        IEntityDataSaver oldDataSaver = ((IEntityDataSaver) oldPlayer);
        IEntityDataSaver newDataSaver = ((IEntityDataSaver) newPlayer);

        newDataSaver.copyFrom(oldDataSaver);
    }
}
