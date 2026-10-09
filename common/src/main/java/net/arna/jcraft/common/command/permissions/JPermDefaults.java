package net.arna.jcraft.common.command.permissions;

import net.minecraft.commands.CommandSourceStack;

import java.util.function.Predicate;

/**
 * Fallbacks used to decide access when no permission manager has an opinion on a {@link JPerm}.
 * <p>
 * These are only defaults; any of them can be overridden per node by a permission manager
 * (LuckPerms on Fabric, whatever implements the Forge permission API on Forge).
 */
public final class JPermDefaults {
    private JPermDefaults() {
    }

    /** Held by everybody, including command blocks and the console. */
    public static final Predicate<CommandSourceStack> ALL = source -> true;

    /** Held by operators (permission level 2), command blocks and the console. */
    public static final Predicate<CommandSourceStack> OP = source -> source.hasPermission(2);

    /** Held by nobody until a permission manager grants it. */
    public static final Predicate<CommandSourceStack> NONE = source -> false;

    /** {@link #OP}, plus anybody hosting their own singleplayer world. */
    @SuppressWarnings("ConstantValue") // lies
    public static final Predicate<CommandSourceStack> OP_OR_SINGLEPLAYER = source -> source.hasPermission(2)
            || (source.getServer() != null && source.getServer().isSingleplayer());
}
