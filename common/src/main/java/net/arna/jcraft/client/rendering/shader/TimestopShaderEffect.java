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
import net.minecraft.world.entity.LivingEntity;
import org.joml.*;
import oshi.util.tuples.Pair;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class TimestopShaderEffect extends ShaderEffect {
    private static final float MAX_RADIUS = 100.f;
    private static final float DURATION = 40.f;
    private static final Matrix4f FROZEN_INV_TRANSFORM_MAT = new Matrix4f();

    private static final List<Pair<LivingEntity, Pair<Float, Float>>> TIMESTOP_SOURCES = new ArrayList<>();

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
        this.program.setUniform("Radius", radius);
//        uniform float OuterSat;
        this.program.setUniform("OuterSat", 1.0f);
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

    public void renderQueuedBubbles(float tickProgress, Camera camera)
    {
        List<Pair<LivingEntity, Pair<Float, Float>>> copied = new ArrayList<>(TIMESTOP_SOURCES);
        for (Pair<LivingEntity, Pair<Float, Float>> source : copied)
        {
            LivingEntity sourceEntity = source.getA();
            float began = source.getB().getA();
            float duration = source.getB().getB();

            float t = Math.min((time-began)/duration, 1.f);

            if (t == 1.f)
            {
                TIMESTOP_SOURCES.remove(source);
                continue;
            }

            float radius;
            if (t < 0.33f) {
                radius = MAX_RADIUS * (t / 0.33f);
            } else if (t > 0.85f) {
                radius = MAX_RADIUS * (1f - (t - 0.85f) / 0.15f);
            } else {
                radius = MAX_RADIUS;
            }

            renderBubble(tickProgress, camera, sourceEntity.position().toVector3f(), radius);
        }
    }

    public void queueBubble(LivingEntity source, float duration)
    {
        TIMESTOP_SOURCES.add(
                new Pair<>(source, new Pair<>(
                        time,       // Began
                        duration    // Lasts
                ))
        );
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