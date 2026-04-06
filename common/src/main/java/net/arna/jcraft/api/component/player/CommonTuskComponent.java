package net.arna.jcraft.api.component.player;

import net.arna.jcraft.api.component.JComponent;

public interface CommonTuskComponent extends JComponent {
    /**
     * Gets the highest Tusk Act unlocked by this player
     * @return 1-4 representing Act 1, Act 2, Act 3, or Act 4
     */
    int getHighestUnlockedAct();

    /**
     * Unlocks a Tusk Act. Can only increase, never decrease.
     * @param act The act to unlock (1-4)
     */
    void unlockAct(int act);

    /**
     * Checks if the player has unlocked a specific act
     * @param act The act to check (1-4)
     * @return true if unlocked
     */
    boolean hasUnlockedAct(int act);

    /**
     * Resets progression back to Act 1
     */
    void resetProgression();
}