package net.arna.jcraft.client.rendering.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.impl.GLBakedProgram;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderSampler;
import net.arna.jcraft.client.rendering.shader.texture.impl.GLShaderTexture;
import net.arna.jcraft.mixin_logic.StillDepthHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TimestopShaderEffect extends ShaderEffect {
    private GLShaderSampler depthSampler;
    private GLShaderSampler colorSampler;

    public TimestopShaderEffect() {
        super(LinkData.vertexFragment(
                JCraft.id("shaders/program/blit.vsh"),
                JCraft.id("shaders/program/timestop.fsh")
        ));
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.depthSampler = new GLShaderSampler((GLBakedProgram) this.program, "DepthSampler");
        this.colorSampler = new GLShaderSampler((GLBakedProgram) this.program, "DiffuseSampler");
    }

    private float time = 0.0f;

    @Override
    public void update(float tickProgress) {
        time += tickProgress;
    }

    public void renderBubble(Camera camera, Vector3f center, float radius)
    {
        Minecraft minecraft = Minecraft.getInstance();

        uniformWriter.reset();

        uniformWriter.pushVec2(
                minecraft.getMainRenderTarget().width,
                minecraft.getMainRenderTarget().height
        );

        uniformWriter.pushFloat(time);

        uniformWriter.write();

//        uniform sampler2D DiffuseSampler;
//        uniform sampler2D DepthSampler;
//
//        uniform vec3 CameraPosition;
        this.program.setUniform("CameraPosition", camera.getPosition().toVector3f());
//        uniform vec3 Center;
        this.program.setUniform("Center", center);

//        uniform float Radius;
        this.program.setUniform("Radius", radius);
//        uniform float OuterSat;
        this.program.setUniform("OuterSat", 1.0f);

//        uniform mat4 InverseTransformMatrix;
        this.program.setUniform("InverseTransformMatrix", getInverseTransformMatrix(new Matrix4f()));

        GLShaderTexture depthTexture = GLShaderTexture.fromGlHandle(((StillDepthHolder) minecraft.getMainRenderTarget()).jcraft$getDepthTexture());
        GLShaderTexture colorTexture = GLShaderTexture.fromGlHandle(minecraft.getMainRenderTarget().getColorTextureId());

        this.depthSampler.bindTexture(depthTexture);
        this.colorSampler.bindTexture(colorTexture);

        this.program.renderFullscreen();
    }

    private static Matrix4f getInverseTransformMatrix(Matrix4f outMat) {
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        outMat.identity();
        outMat.mul(projection);
        outMat.mul(modelView);
        outMat.invert();
        return outMat;
    }
}