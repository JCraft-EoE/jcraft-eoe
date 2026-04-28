package net.arna.jcraft.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.ShaderInstance;

public class JPlatformUtils {
    @ExpectPlatform
    public static ShaderInstance getTest() {
        throw new AssertionError("This shouldn't happen");
    }

    @ExpectPlatform
    public static ShaderInstance getRred() {
        throw new AssertionError("This shouldn't happen");
    }
}
