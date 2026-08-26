package net.arna.jcraft.platform.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;

public class JPermsPlatformImpl {
    public static boolean hasPerm(ServerPlayer player, String permission, boolean defaultValue) {
        // On Fabric, we just delegate the check to the permissions api.
        return Permissions.check(player, permission, defaultValue);
    }
}
