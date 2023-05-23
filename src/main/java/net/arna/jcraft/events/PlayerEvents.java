package net.arna.jcraft.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.arna.jcraft.util.IEntityDataSaver;

// Make sure data that's meant to be persisted isn't wiped when the player dies
public class PlayerEvents implements ServerPlayerEvents.CopyFrom {
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        NbtCompound original = ((IEntityDataSaver) oldPlayer).getPersistentData();
        NbtCompound player = ((IEntityDataSaver) newPlayer).getPersistentData();

        player.putInt("StandID", original.getInt("StandID"));
        player.putInt("SpecID", original.getInt("SpecID"));
    }
}