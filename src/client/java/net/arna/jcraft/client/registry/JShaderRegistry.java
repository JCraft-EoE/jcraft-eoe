package net.arna.jcraft.client.registry;

import com.mojang.datafixers.util.Pair;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderParser;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.PostProcessHandler;
import net.arna.jcraft.client.rendering.post.TimestopShaderPostProcessor;
import net.arna.jcraft.client.rendering.shader.JShader;
import net.arna.jcraft.client.rendering.shader.ShaderHolder;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JShaderRegistry {
    public static List<Pair<ShaderProgram, Consumer<ShaderProgram>>> shaderList;

    //Core
    public static ShaderHolder TEST = new ShaderHolder("DiffuseSampler", "DepthSampler", "OutSize", "ViewPort");

    public static ShaderHolder RREDE = new ShaderHolder();

    //Post Processed
    public static final TimestopShaderPostProcessor ZA_WARUDO = new TimestopShaderPostProcessor();

    public static void init(ResourceFactory manager) throws IOException {
        shaderList = new ArrayList<>();
        registerShader(JShader.createShaderInstance(TEST, manager, JCraft.id("space"), VertexFormats.POSITION_TEXTURE));
        registerShader(JShader.createShaderInstance(RREDE, manager, JCraft.id("rrede"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL));

        PostProcessHandler.addInstance(ZA_WARUDO);
    }

    public static void registerShader(JShader jShaderInstance) {
        registerShader(jShaderInstance, (shader) -> ((JShader) shader).getHolder().setInstance((JShader) shader));
    }

    public static void registerShader(ShaderProgram shader, Consumer<ShaderProgram> onLoaded) {
        shaderList.add(Pair.of(shader, onLoaded));
    }
}
