package net.arna.jcraft.api.misc;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Predicate;

/**
 * Allows for safely setting a player's flight status without fear of it lingering upon state change.
 * E.g., if a stand has a flight move that constantly sets the player's flight status,
 * abruptly cancelling this move might inadvertently leave the player in a state of allowed flight as the move will
 * no longer be ticked after that and can thus not set the player's flight state back. This class seeks to resolve that
 * by always running the check.
 */
// TODO ideally, we'd use Flight API, but it's for 1.21+ only: https://www.curseforge.com/minecraft/mc-mods/flight-api
public class ConditionalFlightHandler {
    private static final List<FlightTracker> TRACKERS = new ArrayList<>();

    @ApiStatus.Internal
    public static void init() {
        TickEvent.SERVER_POST.register(server -> onTick(server.getPlayerList().getPlayers()));

        if (Platform.getEnv() == EnvType.CLIENT)
            ClientTickEvent.CLIENT_POST.register(client -> {
                if (client.player != null) onTick(List.of(client.player));
            });

        registerJCraftConditions();
    }

    private static void registerJCraftConditions() {
        addCondition(
                p -> JUtils.getStand(p) instanceof GEREntity,
                p -> JUtils.getStand(p) instanceof GEREntity ger && ger.getFlightTime() > 0
        );
    }

    /**
     * Adds a flight condition to the list.
     * This method takes two predicates. One determines whether it should even be taken into account.
     * This predicate is used to prevent overriding the player's flight ability
     * @param shouldCheck Whether this condition should be checked
     * @param flightCondition Whether the player may fly
     */
    public static void addCondition(Predicate<Player> shouldCheck, Predicate<Player> flightCondition) {
        TRACKERS.add(new FlightTracker(shouldCheck, flightCondition));
    }

    private static void onTick(Collection<? extends Player> players) {
        for (Player player : players) {
            boolean foundPositiveTracker = false;
            for (FlightTracker tracker : TRACKERS) {
                Boolean flightState = tracker.determineFlightState(player);

                if (flightState == null) continue;

                if (!flightState) {
                    tracker.markInactive(player);
                } else {
                    foundPositiveTracker = true;
                    tracker.markActive(player);
                }

                player.getAbilities().flying = foundPositiveTracker;
            }
        }
    }

    private static class FlightTracker {
        // We track server and local players separately because the LocalPlayer equivalent of a ServerPlayer
        // has the same hashcode and will clash if kept in the same set.
        private final Set<ServerPlayer> activeServerPlayers = Collections.newSetFromMap(new WeakHashMap<>());
        private final Set<Player> activeLocalPlayers = Collections.newSetFromMap(new WeakHashMap<>());
        private final Predicate<Player> shouldCheck, flightCondition;

        private FlightTracker(Predicate<Player> shouldCheck, Predicate<Player> flightCondition) {
            this.shouldCheck = shouldCheck;
            this.flightCondition = flightCondition;
        }

        /**
         * Determines whether this player may fly according to this tracker.
         * @param player The player to check for
         * @return False if the player may not fly, true if they may, null if this tracker doesn't apply to them.
         */
        public Boolean determineFlightState(Player player) {
            if (player.isCreative() || player.isSpectator()) return null;

            if (!shouldCheck.test(player)) {
                if ((player instanceof ServerPlayer ? activeServerPlayers : activeLocalPlayers).contains(player)) {
                    // Player was flying because of this tracker, but this tracker no longer applies to them
                    // so we disable their flight.
                    return false;
                }

                return null;
            }

            return flightCondition.test(player);
        }

        public void markInactive(Player player) {
            if (player instanceof ServerPlayer)
                activeServerPlayers.remove(player);
            else
                activeLocalPlayers.remove(player);
        }

        public void markActive(Player player) {
            if (player instanceof ServerPlayer sp)
                activeServerPlayers.add(sp);
            else
                activeLocalPlayers.add(player);
        }
    }
}
