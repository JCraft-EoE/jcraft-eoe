package net.arna.jcraft.client.rendering.shader.api;

import lombok.Getter;
import net.arna.jcraft.client.rendering.shader.api.uniform.UniformWriter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/// A container for a raw {@link BakedProgram}. Will be reloaded upon a resource reload event if registered.
@Getter
public abstract class ShaderEffect {
    private final LinkData linkData;
    protected BakedProgram program;
    protected UniformWriter uniformWriter;

    protected ShaderEffect(LinkData linkData)
    {
        this.linkData = linkData;
    }

    public abstract void update(float tickProgress);

    @ApiStatus.Internal
    public void link(BakedProgram program)
    {
        this.program = program;
//        this.uniformWriter = new UniformWriter(new UniformWriter.UniformBlock("ShaderUniforms", 0), program);
    }

    public record LinkData(ShaderSourceRef... programMembers) {
        public static LinkData vertexFragment(ResourceLocation vertexPath, ResourceLocation fragmentPath)
        {
            return new LinkData(
                    new ShaderSourceRef(vertexPath, ShaderType.VERTEX),
                    new ShaderSourceRef(fragmentPath, ShaderType.FRAGMENT)
            );
        }
    }
}