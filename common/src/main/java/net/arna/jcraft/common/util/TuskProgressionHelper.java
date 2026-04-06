package net.arna.jcraft.common.util;

import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Helper class for managing Tusk Act progression.
 * Progression is stored in CommonMiscComponent.getHighestTuskAct() (1-based).
 */
public class TuskProgressionHelper {

    /**
     * Unlocks a Tusk Act for the player, fires the advancement trigger, and shows a notification.
     * Only unlocks if the act is higher than their current highest.
     *
     * @param player The player to unlock the act for
     * @param act    The act to unlock (1-4)
     * @return true if act was newly unlocked, false if already unlocked
     */
    public static boolean unlockAct(Player player, int act) {
        CommonMiscComponent misc = JComponentPlatformUtils.getMiscData(player);
        if (misc == null) return false;

        int current = misc.getHighestTuskAct();
        if (act <= current) return false;

        misc.setHighestTuskAct(act);

        // Fire advancement triggers for each newly unlocked act
        if (player instanceof ServerPlayer serverPlayer) {
            if (act >= 2 && current < 2) {
                JAdvancementTriggerRegistry.TUSK_ACT2.trigger(serverPlayer);
            }
            if (act >= 3 && current < 3) {
                JAdvancementTriggerRegistry.TUSK_ACT3.trigger(serverPlayer);
            }
        }

        String actName = "Act " + act;
        player.sendSystemMessage(Component.literal("§6§lTusk " + actName + " Unlocked!"));
        return true;
    }

    /**
     * Checks if a player has unlocked a specific act.
     */
    public static boolean hasUnlockedAct(Player player, int act) {
        CommonMiscComponent misc = JComponentPlatformUtils.getMiscData(player);
        if (misc == null) return act <= 1;
        return act <= misc.getHighestTuskAct();
    }

    /**
     * Returns the highest act the player has unlocked (minimum 1).
     */
    public static int getHighestUnlockedAct(Player player) {
        CommonMiscComponent misc = JComponentPlatformUtils.getMiscData(player);
        if (misc == null) return 1;
        int act = misc.getHighestTuskAct();
        return Math.max(1, act);
    }
}
