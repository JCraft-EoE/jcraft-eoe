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
import org.joml.*;

import java.lang.Math;

public class TimestopShaderEffect extends ShaderEffect {
    private static final float MAX_RADIUS = 100.f;
    private static final float DURATION = 40.f;
    private static final Matrix4f FROZEN_INV_TRANSFORM_MAT = new Matrix4f();

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

        this.depthSampler = new GLShaderSampler((GLBakedProgram) this.program, "DepthSampler", 1);
        this.colorSampler = new GLShaderSampler((GLBakedProgram) this.program, "DiffuseSampler", 0);
    }

    private float time = 0.0f;

    @Override
    public void update(float tickProgress) {
        time += tickProgress;
    }

    public void renderBubble(float tickProgress, Camera camera, Vector3f center, float radius)
    {
        float expansionProgress = (time % DURATION)/DURATION;
        JCraft.LOGGER.info("Time: {} | Expansion Progress: {}", time, expansionProgress);
        float rad = MAX_RADIUS * Math.min(expansionProgress*2.f, 1.f);

        Minecraft minecraft = Minecraft.getInstance();

        this.program.bind();

//        uniformWriter.reset();
//
//        uniformWriter.pushVec2(
//                minecraft.getMainRenderTarget().width,
//                minecraft.getMainRenderTarget().height
//        );
//
//        uniformWriter.pushFloat(time);
//
//        uniformWriter.write();

        this.program.setUniform("Viewport", new Vector2f(minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height));

//        uniform sampler2D DiffuseSampler;
//        uniform sampler2D DepthSampler;
//
//        uniform vec3 CameraPosition;
        this.program.setUniform("CameraPosition", camera.getPosition().toVector3f());
        Quaternionf cameraRot = new Quaternionf(camera.rotation()).conjugate();
        this.program.setUniform("CameraRot", new Vector4f(cameraRot.x, cameraRot.y, cameraRot.z, cameraRot.w));
//        uniform vec3 Center;
        this.program.setUniform("Center", center);

//        uniform float Radius;
        this.program.setUniform("Radius", rad);
//        uniform float OuterSat;
        this.program.setUniform("OuterSat", (expansionProgress <= 0.3f) ? 1.0f : 0.3f);
        this.program.setUniform("FOV", minecraft.gameRenderer.getFov(camera, tickProgress, false));

//        uniform mat4 InverseTransformMatrix;
        this.program.setUniform("InverseTransformMatrix", FROZEN_INV_TRANSFORM_MAT);

        GLShaderTexture colorTexture = GLShaderTexture.fromGlHandle(minecraft.getMainRenderTarget().getColorTextureId());
        GLShaderTexture depthTexture = GLShaderTexture.fromGlHandle(((StillDepthHolder)minecraft.getMainRenderTarget()).jcraft$getDepthTexture());

        this.colorSampler.bindTexture(colorTexture);
        this.depthSampler.bindTexture(depthTexture);

        this.program.renderFullscreen();

        this.program.unbind();
    }

    public static void freezeInvTransformMat() {
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        Matrix4f modelView = RenderSystem.getModelViewMatrix();
        FROZEN_INV_TRANSFORM_MAT.identity();
        FROZEN_INV_TRANSFORM_MAT.mul(projection);
        FROZEN_INV_TRANSFORM_MAT.mul(modelView);
        FROZEN_INV_TRANSFORM_MAT.invert();
    }
}