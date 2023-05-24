package net.arna.jcraft.registry;

import com.mojang.datafixers.util.Pair;
import net.arna.jcraft.client.rendering.shader.JShader;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JShaderRegistry {
    public static List<Pair<Shader, Consumer<Shader>>> shaderList;


    public static void init(ResourceManager manager) throws IOException {
        shaderList = new ArrayList<>();

    }

    public static void registerShader(JShader jShaderInstance) {
        registerShader(jShaderInstance, (shader) -> ((JShader) shader).getHolder().setInstance((JShader) shader));
    }

    public static void registerShader(Shader shader, Consumer<Shader> onLoaded) {
        shaderList.add(Pair.of(shader, onLoaded));
    }
}
