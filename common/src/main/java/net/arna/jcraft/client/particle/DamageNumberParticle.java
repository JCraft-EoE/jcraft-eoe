package net.arna.jcraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arna.jcraft.client.JClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DamageNumberParticle extends Particle {
    private static final List<Float> POSITIONS = new ArrayList<>(Arrays.asList(0f, -0.25f, 0.12f, -0.12f, 0.25f));
    private static final DecimalFormat DF = new DecimalFormat("0.0");

    private final Font fontRenderer = Minecraft.getInstance().font;
    private final Component text;
    private final int color;
    private final int darkColor;
    private float fadeout = -1;
    private float prevFadeout = -1;

    // Visual offset
    private float visualDY = 0;
    private float prevVisualDY = 0;
    private float visualDX = 0;
    private float prevVisualDX = 0;

    public DamageNumberParticle(ClientLevel clientLevel, double x, double y, double z,
                                double damageAmount, double ySpeed, double animationPos) {
        super(clientLevel, x, y, z);
        this.lifetime = 35; // ~1.75 seconds at 20 tps

        // Red color for damage
        this.color = JClientConfig.getInstance().getDummyDamageIndicatorColor();
        this.darkColor = JClientConfig.getInstance().getDummyDamageIndicatorColorShadow();

        this.text = Component.literal(DF.format(Math.abs(damageAmount)));

        this.yd = 0.8; // Initial upward velocity

        // Use the animation position to determine horizontal offset
        int posIndex = (int) animationPos % POSITIONS.size();
        this.xd = POSITIONS.get(posIndex);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float particleX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float particleY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float particleZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int light = LightTexture.FULL_BRIGHT;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(particleX, particleY, particleZ);

        double distanceFromCam = 4f;//new Vec3(particleX, particleY, particleZ).length();
        double inc = Mth.clamp(distanceFromCam / 32f, 0, 5f);

        // Animation
        poseStack.translate(0, (1 + inc / 4f) * Mth.lerp(partialTicks, this.prevVisualDY, this.visualDY), 0);

        float fadeout = Mth.lerp(partialTicks, this.prevFadeout, this.fadeout);

        float defScale = 0.018f; // scale
        float scale = (float) (defScale * distanceFromCam);
        poseStack.mulPose(camera.rotation());

        // Animation
        poseStack.translate((1 + inc) * Mth.lerp(partialTicks, this.prevVisualDX, this.visualDX), 0, 0);
        poseStack.scale(-scale, -scale, scale);
        poseStack.translate(0, (4d * (1 - fadeout)), 0);
        poseStack.scale(fadeout, fadeout, fadeout);
        poseStack.translate(0, -distanceFromCam / 10d, 0);

        var buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);

        float x1 = 0.5f - fontRenderer.width(text) / 2f;

        // Main text
        fontRenderer.drawInBatch(text, x1, 0, color, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);

        // Shadow
        poseStack.translate(1, 1, +0.03);
        fontRenderer.drawInBatch(text, x1, 0, darkColor, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);

        buffer.endBatch();
        poseStack.popPose();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float length = 6;
            this.prevFadeout = this.fadeout;
            this.fadeout = this.age > (lifetime - length) ? ((float) lifetime - this.age) / length : 1;

            this.prevVisualDY = this.visualDY;
            this.visualDY += (float) this.yd;
            this.prevVisualDX = this.visualDX;
            this.visualDX += (float) this.xd;

            // Slow down the particles movement over time
            if (Math.sqrt(Mth.square(this.visualDX * 1.5) + Mth.square(this.visualDY - 1)) < 1.9 - 1) {
                this.yd = this.yd / 2;
            } else {
                this.yd = 0;
                this.xd = 0;
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Factory(SpriteSet spriteSet) {
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z,
                                       double damageAmount, double ySpeed, double zSpeed) {
            return new DamageNumberParticle(worldIn, x, y, z, damageAmount, ySpeed, zSpeed);
        }
    }
}