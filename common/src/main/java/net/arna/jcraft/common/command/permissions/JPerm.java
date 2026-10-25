package net.arna.jcraft.common.command.permissions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.arna.jcraft.platform.JPermsPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

/**
 * A single permission node.
 * <p>
 * Constructing one registers it with {@link JPerms}; Forge walks that registry to build its
 * {@code PermissionNode}s up front, so every node has to be created from {@link JPerms}' static
 * initialiser rather than lazily.
 *
 * @param name     the node itself, e.g. {@code jcraft.stand.set}
 * @param fallback decides access when no permission manager has an opinion on this node
 */
public record JPerm(String name, Predicate<CommandSourceStack> fallback) {
    private static final DynamicCommandExceptionType NO_PERMISSION = new DynamicCommandExceptionType(node ->
            Component.translatable("jcraft.commands.error.no_permission", node));

    public JPerm {
        JPerms.PERMS_MUT.put(name, this);
    }

    /** Creates a node held by operators and the console by default. */
    public JPerm(String name) {
        this(name, JPermDefaults.OP);
    }

    public boolean sourceHas(CommandSourceStack source) {
        return sourceHas(source, fallback.test(source));
    }

    /** As {@link #sourceHas(CommandSourceStack)}, but ignoring {@link #fallback()}. */
    public boolean sourceHas(CommandSourceStack source, boolean defaultValue) {
        if (!source.isPlayer()) return defaultValue;
        return JPermsPlatform.hasPerm(source.getPlayer(), name, defaultValue);
    }

    /**
     * Predicate for {@code .requires(...)}. Brigadier skips nodes a source can't use while parsing,
     * not just while building the tree it sends to clients, so this both hides the node in tab
     * completion and enforces it on execution -- including for every node underneath it.
     */
    public Predicate<CommandSourceStack> require() {
        return this::sourceHas;
    }

    public Predicate<CommandSourceStack> require(boolean defaultValue) {
        return source -> sourceHas(source, defaultValue);
    }

    /**
     * Throws if the source doesn't hold this node. For checks that {@code .requires(...)} can't
     * express because they depend on the parsed arguments, such as {@link JPerms#checkTargets}.
     */
    public void check(CommandSourceStack source) throws CommandSyntaxException {
        if (!sourceHas(source)) throw NO_PERMISSION.create(name);
    }
}
