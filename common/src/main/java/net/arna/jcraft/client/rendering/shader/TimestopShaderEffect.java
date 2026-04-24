package net.arna.jcraft.client.rendering.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.api.callbacks.PostShaderRenderCallback;
import net.arna.jcraft.client.rendering.shader.api.BakedProgram;
import net.arna.jcraft.client.rendering.shader.api.ShaderEffect;
import net.arna.jcraft.client.rendering.shader.api.uniform.UniformWriter;
import net.arna.jcraft.client.rendering.shader.texture.api.ShaderSampler;
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

public class TimestopShaderEffect extends ShaderEffect implements PostShaderRenderCallback {
    private static final float MAX_RADIUS = 100.f;
    private static final Matrix4f FROZEN_INV_TRANSFORM_MAT = new Matrix4f();

    private static final List<Pair<LivingEntity, Pair<Float, Float>>> TIMESTOP_SOURCES = new ArrayList<>();

    private ShaderSampler depthSampler;
    private ShaderSampler colorSampler;

    public TimestopShaderEffect() {
        super(new LinkData(
                JCraft.id("shaders/program/blit.vsh"),
                JCraft.id("shaders/program/timestop.fsh")
        ));

        linkData.addSampler("DiffuseSampler");
        linkData.addSampler("DepthSampler");

        linkData.freeze();

        PostShaderRenderCallback.EVENT.register(this);
    }

    @Override
    public void link(BakedProgram program) {
        super.link(program);

        this.colorSampler = getSampler("DiffuseSampler");
        this.depthSampler = getSampler("DepthSampler");
    }

    private float time = 0.0f;

    @Override
    public void update(float tickProgress) {
        time += tickProgress;
    }

    public void renderBubble(float tickProgress, Camera camera, Vector3f center, float desatRadius, float radius, float distortionMul, float hueOffset)
    {
        Minecraft minecraft = Minecraft.getInstance();

        this.program.pass(() -> {
            setUniforms(camera, center, desatRadius, radius, distortionMul, hueOffset);

            GLShaderTexture colorTexture = GLShaderTexture.fromGlHandle(minecraft.getMainRenderTarget().getColorTextureId());
            GLShaderTexture depthTexture = GLShaderTexture.fromGlHandle(((StillDepthHolder)minecraft.getMainRenderTarget()).jcraft$getDepthTexture());

            this.colorSampler.bindTexture(colorTexture);
            this.depthSampler.bindTexture(depthTexture);

            this.program.renderFullscreen();
        });
    }

    private void setUniforms(Camera camera, Vector3f center, float desatRadius, float radius, float distortionMul, float hueOffset)
    {
        /* == SHADER UNIFORMS ==
         * vec3 CameraPosition;
         * vec4 CameraRotation;
         * vec3 Center;
         * float DesatRadius;
         * float Radius;
         * float DistortionMul
         * float HueOffset;
         * float OuterSat;
         * mat4 InverseTransformMatrix;
         * vec2 Viewport;
         * */

        Minecraft minecraft = Minecraft.getInstance();

        this.program.setUniform("CameraPosition", camera.getPosition().toVector3f());

        Quaternionf cameraRot = new Quaternionf(camera.rotation()).normalize().conjugate().normalize();
        this.program.setUniform("CameraRotation", new Vector4f(cameraRot.x, cameraRot.y, cameraRot.z, cameraRot.w));

        this.program.setUniform("Center", center);
        this.program.setUniform("DesatRadius", desatRadius);
        this.program.setUniform("Radius", radius);
        this.program.setUniform("DistortionMul", distortionMul);
        this.program.setUniform("HueOffset", hueOffset);
        this.program.setUniform("OuterSat", 1.0f);

        this.program.setUniform("InverseTransformMatrix", FROZEN_INV_TRANSFORM_MAT);
        this.program.setUniform("Viewport", new Vector2f(minecraft.getMainRenderTarget().width, minecraft.getMainRenderTarget().height));
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

    @Override
    public void renderEffect(float tickProgress) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();

        List<Pair<LivingEntity, Pair<Float, Float>>> copied = new ArrayList<>(TIMESTOP_SOURCES);
        for (Pair<LivingEntity, Pair<Float, Float>> source : copied)
        {
            LivingEntity sourceEntity = source.getA();
            float began = source.getB().getA();
            float duration = source.getB().getB();

            final float maxOvertime = 1.48f;
            float t = Math.min((time-began)/duration, maxOvertime);

            if (t == maxOvertime)
            {
                TIMESTOP_SOURCES.remove(source);
                continue;
            }

            // TODO: this code is so sloppy and needs to be redone
            float radius = 0f, desatRadius = 0f, distortionMul = 1f;
            if (t < 0.33f) {
                radius = MAX_RADIUS * (t / 0.33f);
                desatRadius = radius;
                distortionMul = (1.f-(t/0.33f));
            } else {
                desatRadius = MAX_RADIUS;
                radius = 0.f;
                distortionMul = 0.f;
            }

            if (t >= 0.33f && t <= 0.7f) {
                radius = MAX_RADIUS*(1.f-((t-0.33f)/0.37f));
            }

            radius      = MAX_RADIUS*(float)(1.f-Math.pow(2.f, -10.f*(radius/MAX_RADIUS)));
            desatRadius = MAX_RADIUS*(float)(1.f-Math.pow(2.f, -10.f*(desatRadius/MAX_RADIUS)));

            renderBubble(tickProgress, camera, sourceEntity.position().toVector3f(), desatRadius, radius, distortionMul, t/maxOvertime);
        }
    }
}