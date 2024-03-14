package net.arna.jcraft.common.callbacks;

import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Callback called when a player inputs a move.
 */
public interface JServerPlayerInputCallback {
    Event<JServerPlayerInputCallback> EVENT = EventFactory.createArrayBacked(JServerPlayerInputCallback.class,
            listeners -> (player, moveInput, pressed, moveSuccess) -> {
                for (JServerPlayerInputCallback listener : listeners)
                    listener.onPlayerInput(player, moveInput, pressed, moveSuccess);
            });

    /**
     * Called when a player inputs a move.
     * @param player The player that input the move
     * @param moveInput The move the player input
     * @param pressed Whether the move was pressed or released
     * @param moveSuccess Whether the move was successful
     */
    void onPlayerInput(ServerPlayerEntity player, MoveInputType moveInput, boolean pressed, boolean moveSuccess);
}
