package net.arna.jcraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.arna.jcraft.client.rendering.handler.SpecialParticleShaderHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("deprecation") // Minecraft uses it too
@Environment(EnvType.CLIENT)
public class JParticleTextureSheet {
    public static final ParticleRenderType PARTICLE_SHEET_AURA = new ParticleRenderType() {
        public void begin(final BufferBuilder builder, final TextureManager textureManager) {
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(final Tesselator tessellator) {
            tessellator.end();
        }

        public String toString() {
            return "PARTICLE_SHEET_AURA";
        }
    };

    public static final ParticleRenderType INVERSION_SHEET = new ParticleRenderType() {
        public void begin(final BufferBuilder builder, final @NotNull TextureManager textureManager) {
            // Doesn't seem to work by using a blend function, so we'll use a shader instead.
            // Think that is because of the render order, but I'm not sure.
            SpecialParticleShaderHandler.getToInvertBuffer().copyDepthFrom(Minecraft.getInstance().getMainRenderTarget()); // Copy depth buffer
            SpecialParticleShaderHandler.getToInvertBuffer().bindWrite(true); // Render to inversion buffer

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(final Tesselator tessellator) {
            tessellator.end();

            Minecraft.getInstance().getMainRenderTarget().bindWrite(true); // Revert to the main buffer
        }

        public String toString() {
            return "INVERSION_SHEET";
        }
    };

    public static final ParticleRenderType OVERLAP_SHEET = new ParticleRenderType() {
        public void begin(final BufferBuilder builder, final @NotNull TextureManager textureManager) {
            SpecialParticleShaderHandler.getOverlapBuffer().bindWrite(true); // Render to overlap buffer

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest(); // No depth
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(final Tesselator tessellator) {
            tessellator.end();

            Minecraft.getInstance().getMainRenderTarget().bindWrite(true); // Revert to the main buffer
        }

        public String toString() {
            return "OVERLAP_SHEET";
        }
    };

    public static final List<ParticleRenderType> J_SHEETS = ImmutableList.of(INVERSION_SHEET, OVERLAP_SHEET, PARTICLE_SHEET_AURA);
}
