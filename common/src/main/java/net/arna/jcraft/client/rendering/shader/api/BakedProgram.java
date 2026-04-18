package net.arna.jcraft.client.rendering.shader.api;

import lombok.Getter;
import net.arna.jcraft.client.rendering.shader.except.IllegalRenderCommandException;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
import org.jetbrains.annotations.Nullable;

/// Compiled graphics API agnostic shader program. Made using an implementation of {@link JShaderProvider} (i.e. {@link net.arna.jcraft.client.rendering.shader.impl.GLShaderProvider}) and two or more {@link UnbakedShader}s.
public abstract class BakedProgram {
    @Getter
    private static @Nullable BakedProgram boundProgram;
    private boolean bound = false;
    public final String name;

    protected BakedProgram(String name)
    {
        this.name = name;
    }

    /// Unbind the shader program from rendering.<br><br>
    /// <b>IMPLEMENTATION NOTE</b>: This delegates to {@link #bindProgram()}.
    public final void bind()
    {
        boundProgram = this;
        bound = true;
        bindProgram();
    }
    /// Unbind the shader program from rendering.<br><br>
    /// <b>IMPLEMENTATION NOTE</b>: This delegates to {@link #unbindProgram()}.
    public final void unbind()
    {
        unbindProgram();
        boundProgram = null;
        bound = false;
    }

    protected abstract void bindProgram(); /* Implementation of 'bind' */
    protected abstract void unbindProgram(); /* Implementation of 'unbind' */

    /// Helper function for binding this shader, rendering a pass, and unbinding.
    public void pass(Runnable pass)
    { bind(); pass.run(); unbind(); }

    public abstract void renderFullscreen();

    public abstract int handle();

    public abstract int getUniformLocation(String name);
    public abstract <T> @Nullable T getUniform(Class<T> type, String name);
    public abstract void setUniform(String name, Object type);

    public abstract @Nullable ShaderSampler initializeSampler(String name, int index);

    protected final void requireBound(String operation)
    {
        if (!bound)
            throw new IllegalRenderCommandException("Shader (of type " + this.getClass().getSimpleName() + ") has to be bound for " + operation + ".");
    }
}