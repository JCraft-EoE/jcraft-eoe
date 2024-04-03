package net.arna.jcraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.client.rendering.handler.InversionShaderHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;

import java.util.List;

@SuppressWarnings("deprecation") // Minecraft uses it too
@Environment(EnvType.CLIENT)
public class JParticleTextureSheet {
    public static final ParticleTextureSheet PARTICLE_SHEET_AURA = new ParticleTextureSheet() {
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);

            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public void draw(Tessellator tessellator) {
            tessellator.draw();
        }

        public String toString() {
            return "PARTICLE_SHEET_AURA";
        }
    };

    public static final ParticleTextureSheet INVERSION_SHEET = new ParticleTextureSheet() {
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            // Doesn't seem to work by using a blend function, so we'll use a shader instead.
            // Think that is because of the render order, but I'm not sure.
            InversionShaderHandler.getToInvertBuffer().copyDepthFrom(MinecraftClient.getInstance().getFramebuffer()); // Copy depth buffer
            InversionShaderHandler.getToInvertBuffer().beginWrite(true); // Render to inversion buffer

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleProgram);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);

            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        }

        public void draw(Tessellator tessellator) {
            tessellator.draw();

            MinecraftClient.getInstance().getFramebuffer().beginWrite(true); // Revert to the main buffer
        }

        public String toString() {
            return "INVERSION_SHEET";
        }
    };

    public static final List<ParticleTextureSheet> J_SHEETS = ImmutableList.of(INVERSION_SHEET, PARTICLE_SHEET_AURA);
}
