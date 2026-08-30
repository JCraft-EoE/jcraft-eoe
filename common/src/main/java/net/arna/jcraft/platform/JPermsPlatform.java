package net.arna.jcraft.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public class JPermsPlatform {

    @ExpectPlatform
    public static boolean hasPerm(ServerPlayer player, String permission, boolean defaultValue) {
        throw new AssertionError("Shouldn't happen");
    }
}
