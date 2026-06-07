package net.arna.jcraft.api.component.living;

import net.arna.jcraft.api.component.JComponent;
import net.arna.jcraft.common.util.CooldownType;

public interface CommonCooldownsComponent extends JComponent {
    /**
     * Gets the current cooldown in ticks for the given type.
     *
     * @param type The type to check for
     * @return the current cooldown in ticks for the given type
     */
    int getCooldown(final CooldownType type);

    /**
     * Returns the initial duration for the given type.
     * When {@link #setCooldown(CooldownType, int)} is called, this value is updated.
     *
     * @param type The type to get the initial duration for.
     * @return The initial duration for the given type.
     */
    int getInitialDuration(final CooldownType type);

    /**
     * Sets the cooldown for the given type.
     *
     * @param type     The type to set a cooldown for
     * @param duration The duration of the cooldown in ticks
     */
    void setCooldown(final CooldownType type,final  int duration);

    default void startCooldown(final CooldownType type) {
        if (type.getDuration() < 0) {
            throw new IllegalArgumentException("Given type has no default duration. " +
                    "Please use #setCooldown(CooldownType, int)");
        }
        setCooldown(type, type.getDuration());
    }

    /**
     * Clears all cooldowns with some sparkles and effects.
     */
    void cooldownCancel();

    /**
     * Removes the cooldown of the given type.
     *
     * @param type The type to remove
     */
    void clear(final CooldownType type);

    /**
     * Simply clears all cooldowns.
     * Same as {@link #cooldownCancel()}, but without the sparkles.
     */
    void clear();
}
