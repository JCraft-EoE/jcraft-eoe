package net.arna.jcraft.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.arna.jcraft.client.rendering.api.MultiInstancePostProcessor;
import net.arna.jcraft.client.rendering.post.TimestopShaderFX;
import net.minecraft.client.renderer.ShaderInstance;

public class JPlatformUtils {

    @ExpectPlatform
    public static MultiInstancePostProcessor<TimestopShaderFX> getZaWarudo(){
        throw new AssertionError("This shouldn't happen");
    }

    @ExpectPlatform
    public static ShaderInstance getTest() {
        throw new AssertionError("This shouldn't happen");
    }

    @ExpectPlatform
    public static ShaderInstance getRred() {
        throw new AssertionError("This shouldn't happen");
    }

    @ExpectPlatform
    public static boolean isModLoaded(String name) {
        throw new AssertionError("This shouldn't happen");
    }
}
