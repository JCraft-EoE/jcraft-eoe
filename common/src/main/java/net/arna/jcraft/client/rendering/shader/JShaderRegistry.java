package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.*;
import net.arna.jcraft.client.rendering.shader.impl.GLShaderProvider;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JShaderRegistry {
    private static final JShaderProvider<? extends BakedProgram> SHADER_PROVIDER = new GLShaderProvider();
    private static final List<ShaderEffect> RELOADABLE_SHADER_EFFECTS = new ArrayList<>();

    public static @Nullable BasicShaderEffect BASIC_PROGRAM = null;
    private static ShaderSourceProvider sourceProvider = null;
    private static ShaderPreprocessor preprocessor = null;
    public static @Nullable TimestopShaderEffect TIMESTOP_EFFECT = null;

    public static void init()
    {
        BASIC_PROGRAM = register(new BasicShaderEffect(JCraft.id("shaders/program/blit.vsh"), JCraft.id("shaders/program/basic.fsh")));
        TIMESTOP_EFFECT = register(new TimestopShaderEffect());
    }

    private static void loadShaders(ResourceManager resourceManager)
    {
        sourceProvider = new ShaderSourceProvider(resourceManager);
        preprocessor = new ShaderPreprocessor(resourceManager);

        compileReloadable();
    }

    public static CompletableFuture<Void> onReload(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                   ResourceManager resourceManager,
                                                   ProfilerFiller preparationsProfiler,
                                                   ProfilerFiller reloadProfiler,
                                                   Executor backgroundExecutor,
                                                   Executor gameExecutor) {
        return CompletableFuture.runAsync(()->loadShaders(resourceManager), gameExecutor)
                                .thenCompose(preparationBarrier::wait);
    }

    /// Registers a {@link ShaderEffect} to be compiled and to be reloadable.
    private static <T extends ShaderEffect> T register(T effect)
    {
        RELOADABLE_SHADER_EFFECTS.add(effect);
        return effect;
    }

    private static void compileReloadable()
    {
        for (ShaderEffect effect : RELOADABLE_SHADER_EFFECTS)
        {
            ShaderEffect.LinkData linkData = effect.getLinkData();

            UnbakedShader[] processed = new UnbakedShader[linkData.programMembers().length];
            for (int i = 0; i < linkData.programMembers().length; i++) {
                ShaderSourceRef sourceRef = linkData.programMembers()[i];
                UnbakedShader source = sourceProvider.loadSource(sourceRef);

                processed[i] = preprocessor.process(source);
            }

            effect.link(SHADER_PROVIDER.compile(processed));
        }
    }
}
