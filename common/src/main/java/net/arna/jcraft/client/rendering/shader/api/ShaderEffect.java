package net.arna.jcraft.client.rendering.shader.api;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.uniform.UniformWriter;
import net.arna.jcraft.client.rendering.shader.except.ShaderLinkDataException;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/// A container for a raw {@link BakedProgram}. Will be reloaded upon a resource reload event if registered.
@Getter
public abstract class ShaderEffect {
    protected final LinkData linkData;
    protected BakedProgram program;
    private Map<String, UniformWriter> uniformWriters;
    private Map<String, ShaderSampler> samplers;

    protected ShaderEffect()
    {
        this(new LinkData());
    }

    protected ShaderEffect(LinkData data)
    {
        this.linkData = data;
    }

    public abstract void update(float tickProgress);

    @ApiStatus.Internal
    public void link(BakedProgram program)
    {
        if (program == null)
        { throw new RuntimeException("Cannot link a null baked program to a shader effect! Did it fail to compile?"); }

        this.program = program;

        List<String> uniformBlocks = this.linkData.getUniformBuffers();
        if (!uniformBlocks.isEmpty())
        {
            int iota = 0;
            this.uniformWriters = new HashMap<>(uniformBlocks.size());

            for (String name : uniformBlocks) {
                this.uniformWriters.put(name, new UniformWriter(
                        new UniformWriter.UniformBlock(name, iota++),
                        program
                ));
            }
        }

        List<String> linkSamplers = this.linkData.getSamplers();
        if (!linkSamplers.isEmpty())
        {
            int iota = 0;
            this.samplers = new HashMap<>(linkSamplers.size());

            for (String name : linkSamplers) {
                ShaderSampler sampler = program.initializeSampler(name, iota++);
                if (sampler == null)
                {
                    JCraft.LOGGER.warn("Sampler '{}' was not found/unused in shader '{}'", name, program.name);
                    continue;
                }

                this.samplers.put(name, sampler);
            }
        }
    }

    public @Nullable UniformWriter getUniformWriter(String name)
    {
        return uniformWriters.getOrDefault(name, null);
    }

    public @Nullable ShaderSampler getSampler(String name)
    {
        return samplers.getOrDefault(name, null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class LinkData {
        private List<ShaderSourceRef> sources = new ArrayList<>();
        private List<String> uniformBuffers = new ArrayList<>();
        private List<String> samplers = new ArrayList<>();
        private boolean frozen = false;

        public LinkData()
        { }

        public LinkData(ResourceLocation vertexPath, ResourceLocation fragmentPath)
        {
            this(
                    new ShaderSourceRef(vertexPath, ShaderType.VERTEX),
                    new ShaderSourceRef(fragmentPath, ShaderType.FRAGMENT)
            );
        }

        public LinkData(ShaderSourceRef... sources)
        {
            this.sources.addAll(List.of(sources));
        }

        public LinkData addUniformBuffer(String name)
        {
            checkNotFrozen();

            uniformBuffers.add(name);
            return this;
        }

        public LinkData addSource(ResourceLocation path, ShaderType type)
        {
            return this.addSource(
                    new ShaderSourceRef(path, type)
            );
        }

        public LinkData addSource(ShaderSourceRef ref)
        {
            checkNotFrozen();
            this.sources.add(ref);
            return this;
        }

        public LinkData addSampler(String name)
        {
            checkNotFrozen();
            this.samplers.add(name);
            return this;
        }

        public List<ShaderSourceRef> getSources() {
            checkFrozen();
            return sources;
        }

        public List<String> getUniformBuffers() {
            checkFrozen();
            return uniformBuffers;
        }

        public List<String> getSamplers() {
            checkFrozen();
            return samplers;
        }

        public void freeze()
        {
            if (frozen) return;

            frozen = true;

            sources         = Collections.unmodifiableList(sources);
            uniformBuffers  = Collections.unmodifiableList(uniformBuffers);
            samplers        = Collections.unmodifiableList(samplers);
        }

        private void checkNotFrozen()
        {
            if (frozen)
            { throw new RuntimeException("Shader is already linked but LinkData was modified."); }
        }

        private void checkFrozen()
        {
            if (!frozen)
            { throw new RuntimeException("Attempted to get the link data of shader before it was frozen."); }
        }
    }
}