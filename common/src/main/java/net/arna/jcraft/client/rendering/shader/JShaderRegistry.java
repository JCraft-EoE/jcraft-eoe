package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.*;
import net.arna.jcraft.client.rendering.shader.impl.GLShaderProvider;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JShaderRegistry {
    private static final JShaderProvider<? extends BakedProgram> SHADER_PROVIDER = new GLShaderProvider();

    private static Map<String, ShaderEffect> RELOADABLE_SHADER_EFFECTS = new HashMap<>();
    private static boolean frozen = false;
    private static ShaderSourceProvider sourceProvider = null;
    private static ShaderPreprocessor preprocessor = null;

    public static @Nullable BasicShaderEffect BASIC_PROGRAM = null;
    public static @Nullable TimestopShaderEffect TIMESTOP_EFFECT = null;
    public static @Nullable TimeEraseShaderEffect TIME_ERASE = null;
    public static @Nullable EpitaphVignetteShaderEffect EPITAPH_VIGNETTE = null;
    public static @Nullable SpecialParticleShaderEffect INVERSION = null;
    public static @Nullable SpecialParticleShaderEffect OVERLAP = null;

    public static void init()
    {
        BASIC_PROGRAM    = register("Basic", new BasicShaderEffect(JCraft.id("shaders/program/blit.vsh"), JCraft.id("shaders/program/basic.fsh")));
        TIMESTOP_EFFECT  = register("Timestop", new TimestopShaderEffect());
        TIME_ERASE       = register("Time Erase", new TimeEraseShaderEffect());
        EPITAPH_VIGNETTE = register("Epitaph Vignette", new EpitaphVignetteShaderEffect());
        INVERSION        = register("Inversion", new SpecialParticleShaderEffect(JCraft.id("shaders/program/invert.fsh")));
        OVERLAP          = register("Overlap", new SpecialParticleShaderEffect(JCraft.id("shaders/program/overlap.fsh")));

        freezeRegistry();
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
    private static <T extends ShaderEffect> T register(String name, T effect)
    {
        if (frozen)
            throw new RuntimeException("Cannot register shader after registry is frozen.");

        RELOADABLE_SHADER_EFFECTS.put(name, effect);
        return effect;
    }

    private static void compileReloadable()
    {
        if (!frozen)
            throw new RuntimeException("Cannot compile shaders while registry is not frozen.");

        for (Map.Entry<String, ShaderEffect> entry : RELOADABLE_SHADER_EFFECTS.entrySet())
        {
            String name         = entry.getKey();
            ShaderEffect effect = entry.getValue();

            ShaderEffect.LinkData linkData = effect.getLinkData();

            int sourceCount = linkData.getSources().size();
            UnbakedShader[] processed = new UnbakedShader[sourceCount];
            for (int i = 0; i < sourceCount; i++) {
                ShaderSourceRef sourceRef = linkData.getSources().get(i);
                UnbakedShader source = sourceProvider.loadSource(sourceRef);

                processed[i] = preprocessor.process(source);
            }

            effect.link(SHADER_PROVIDER.compile(name, processed));
        }
    }

    private static void freezeRegistry()
    {
        if (frozen) return;
        frozen = true;

        RELOADABLE_SHADER_EFFECTS = Map.copyOf(RELOADABLE_SHADER_EFFECTS);
    }
}
