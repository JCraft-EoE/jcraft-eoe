package net.arna.jcraft.client.rendering.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.texture.SpriteAtlasTexture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * So we can get more uniforms for our CORE shaders
 */
public class ShaderHolder {
    public JShader instance;
    public ArrayList<String> uniforms;
    public ArrayList<UniformData> defaultUniformData = new ArrayList<>();
    public final RenderPhase.ShaderProgram phase = new RenderPhase.ShaderProgram(getInstance());

    public ShaderHolder(String... uniforms) {
        this.uniforms = new ArrayList<>(List.of(uniforms));
    }

    public void setUniformDefaults() {
        RenderSystem.setShaderTexture(1, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        defaultUniformData.forEach(u -> u.setUniformValue(instance.getUniformOrDefault(u.uniformName)));
    }

    public void setInstance(JShader instance) {
        this.instance = instance;
    }

    public Supplier<ShaderProgram> getInstance() {
        return () -> instance;
    }
}