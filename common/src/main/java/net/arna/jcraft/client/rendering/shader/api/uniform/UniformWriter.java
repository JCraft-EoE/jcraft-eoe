package net.arna.jcraft.client.rendering.shader.api.uniform;

import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.except.IllegalRenderCommandException;
import net.arna.jcraft.client.rendering.shader.except.IllegalShaderUniformsException;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL33C.*;

/// Helper class for writing to std140 layout uniform buffers.
public class UniformWriter {
    private final UniformBlock uniformBlock;
    private final List<Float> buffer = new ArrayList<>();

    public UniformWriter(UniformBlock block, BakedProgram program) {
        this.uniformBlock = block;
        block.load(program);
    }

    public void reset() {
        buffer.clear();
    }

    private void align(int alignmentBytes) {
        int byteSize = buffer.size() * 4;
        int padding = (alignmentBytes - (byteSize % alignmentBytes)) % alignmentBytes;
        int floatsToAdd = padding / 4;

        for (int i = 0; i < floatsToAdd; i++) {
            buffer.add(0f);
        }
    }

    public void pushFloat(float val) {
        align(4);
        buffer.add(val);
    }

    public void pushVec2(Vector2f val) {
        pushVec2(val.x, val.y);
    }

    public void pushVec3(Vector3f val) {
        pushVec3(val.x, val.y, val.z);
    }

    public void pushVec4(Vector4f val) {
        pushVec4(val.x, val.y, val.z, val.w);
    }

    public void pushVec2(float x, float y) {
        align(8);
        buffer.add(x);
        buffer.add(y);
    }

    public void pushVec3(float x, float y, float z) {
        align(16);
        buffer.add(x);
        buffer.add(y);
        buffer.add(z);
        buffer.add(0f);
    }

    public void pushVec4(float x, float y, float z, float w) {
        align(16);
        buffer.add(x);
        buffer.add(y);
        buffer.add(z);
        buffer.add(w);
    }

    public void pushMat4(Matrix4f val)
    {
        align(16);

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(16);
            val.get(buffer);

            for (int i = 0; i < 16; i++)
            { this.buffer.add(buffer.get(i)); }
        }
    }

    private final Map<Class<?>, Consumer<Object>> PUSH_FUNCTIONS = Map.of(
            Float.class,    (val -> pushFloat((float)val)),

            Vector2f.class, (val -> pushVec2((Vector2f) val)),
            Vector3f.class, (val -> pushVec3((Vector3f) val)),
            Vector4f.class, (val -> pushVec4((Vector4f) val)),

            Matrix4f.class, (val -> pushMat4((Matrix4f)val))
    );

    public <T> void push(T val)
    {
        Consumer<Object> pushFunc = PUSH_FUNCTIONS.getOrDefault(val.getClass(), null);
        if (pushFunc == null)
            throw new IllegalRenderCommandException("No uniform buffer push function exists for object '" + val.getClass().getSimpleName() + "'");

        pushFunc.accept(val);
    }

    public void write() {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(buffer.size());

        for (float f : buffer)
        { floatBuffer.put(f); }

        floatBuffer.flip();

        glBindBuffer(GL_UNIFORM_BUFFER, uniformBlock.handle);
        glBufferData(GL_UNIFORM_BUFFER, floatBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glBindBufferRange(GL_UNIFORM_BUFFER, uniformBlock.bindingPoint, uniformBlock.handle, 0, floatBuffer.capacity() * 4L);
    }

    public static class UniformBlock {
        private final String name;
        private final int bindingPoint;

        private int handle;

        public UniformBlock(String name, int bindingPoint) {
            this.name = name;
            this.bindingPoint = bindingPoint;
        }

        private void load(BakedProgram program) {
            int index = glGetUniformBlockIndex(program.handle(), name);

            if (index == GL_INVALID_INDEX) {
                throw new IllegalShaderUniformsException("Could not find uniform block '" + name + "' in shader!");
            }

            handle = glGenBuffers();

            glUniformBlockBinding(program.handle(), index, bindingPoint);
        }
    }
}