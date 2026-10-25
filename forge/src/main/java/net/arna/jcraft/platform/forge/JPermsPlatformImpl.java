package net.arna.jcraft.platform.forge;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.command.permissions.JPerm;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContextKey;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class JPermsPlatformImpl {
    private static final Map<String, PermissionNode<Boolean>> NODES = new HashMap<>();
    private static final PermissionDynamicContextKey<Boolean> DEFAULT_VALUE = new PermissionDynamicContextKey<>(Boolean.class, "jdefval", Object::toString);

    public static void gatherNodes(Consumer<Collection<? extends PermissionNode<?>>> consumer) {
        for (JPerm perm : JPerms.PERMS.values()) {
            String strippedName = perm.name();
            if (strippedName.startsWith("jcraft."))
                strippedName = strippedName.substring("jcraft.".length());

            NODES.put(perm.name(), createBool(strippedName));
        }

        consumer.accept(NODES.values());
    }

    private static PermissionNode<Boolean> createBool(String name) {
        return new PermissionNode<>(JCraft.id(name), PermissionTypes.BOOLEAN,
                JPermsPlatformImpl::retrieveDefaultValue, DEFAULT_VALUE);
    }

    private static boolean retrieveDefaultValue(ServerPlayer player, UUID uuid, PermissionDynamicContext<?>... context) {
        for (PermissionDynamicContext<?> ctx : context) {
            if (ctx.getDynamic() == DEFAULT_VALUE)
                return (Boolean) ctx.getValue();
        }

        // Should be impossible for any of our permissions.
        return false;
    }

    public static boolean hasPerm(ServerPlayer player, String permission, boolean defaultValue) {
        // On Forge, we gotta do a whole rigamarole with registering stuff.
        PermissionNode<Boolean> node = NODES.get(permission);
        // Only happens for a JPerm created after PermissionGatherEvent, i.e. one outside of JPerms.
        if (node == null) return defaultValue;
        return PermissionAPI.getPermission(player, node, DEFAULT_VALUE.createContext(defaultValue));
    }
}
