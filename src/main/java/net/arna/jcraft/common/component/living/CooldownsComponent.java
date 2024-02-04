package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.arna.jcraft.common.util.CooldownType;

public interface CooldownsComponent extends Component, AutoSyncedComponent, CommonTickingComponent {
    /**
     * Gets the current cooldown in ticks for the given type.
     * @param type The type to check for
     * @return the current cooldown in ticks for the given type
     */
    int getCooldown(CooldownType type);

    /**
     * Returns the initial duration for the given type.
     * When {@link #setCooldown(CooldownType, int)} is called, this value is updated.
     * @param type The type to get the initial duration for.
     * @return The initial duration for the given type.
     */
    int getInitialDuration(CooldownType type);

    /**
     * Sets the cooldown for the given type.
     * @param type The type to set a cooldown for
     * @param duration The duration of the cooldown
     */
    void setCooldown(CooldownType type, int duration);

    default void startCooldown(CooldownType type) {
        if (type.getDuration() < 0) throw new IllegalArgumentException("Given type has no default duration. " +
                "Please use #setCooldown(CooldownType, int)");
        setCooldown(type, type.getDuration());
    }

    /**
     * Clears all cooldowns with some sparkles and effects.
     */
    void cooldownCancel();

    /**
     * Removes the cooldown of the given type.
     * @param type The type to remove
     */
    void clear(CooldownType type);

    /**
     * Simply clears all cooldowns.
     * Same as {@link #cooldownCancel()}, but without the sparkles.
     */
    void clear();
}
