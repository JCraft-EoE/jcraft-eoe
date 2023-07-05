package net.arna.jcraft.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class RenderUtils {

    public static final int FULL_BRIGHT = 15728880;

    public static Shader getShader(RenderLayer type) {
        if (type instanceof RenderLayer.MultiPhase compositeRenderType) {
            Optional<Supplier<Shader>> shader = compositeRenderType.phases.shader.supplier;
            if (shader.isPresent()) {
                return shader.get().get();
            }
        }
        return null;
    }

    //TODO: get sterner to explain this
    public static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex((x + 0), (double)(y + 0), 0.0).color(red, green, blue, alpha).next();
        buffer.vertex((x + 0), (double)(y + height), 0.0).color(red, green, blue, alpha).next();
        buffer.vertex((double)(x + width), (double)(y + height), 0.0).color(red, green, blue, alpha).next();
        buffer.vertex((double)(x + width), (double)(y + 0), 0.0).color(red, green, blue, alpha).next();
        BufferRenderer.drawWithShader(buffer.end());
    }

    public static void vertexPos(VertexConsumer vertexConsumer, Matrix4f last, float x, float y, float z) {
        vertexConsumer.vertex(last, x, y, z).next();
    }

    public static void vertexPosUV(VertexConsumer vertexConsumer, Matrix4f last, float x, float y, float z, float u, float v) {
        vertexConsumer.vertex(last, x, y, z).texture(u, v).next();
    }

    public static void vertexPosUVLight(VertexConsumer vertexConsumer, Matrix4f last, float x, float y, float z, float u, float v, int light) {
        vertexConsumer.vertex(last, x, y, z).texture(u, v).light(light).next();
    }

    public static void vertexPosColor(VertexConsumer vertexConsumer, Matrix4f last, float x, float y, float z, float r, float g, float b, float a) {
        vertexConsumer.vertex(last, x, y, z).color(r, g, b, a).next();
    }

    public static void vertexPosColorUV(VertexConsumer vertexConsumer, Matrix4f last, float x, float y, float z, float r, float g, float b, float a, float u, float v) {
        vertexConsumer.vertex(last, x, y, z).color(r, g, b, a).texture(u, v).next();
    }

    public static void vertexPosColorUVLight(VertexConsumer vertexConsumer, Matrix4f last, float x, float y, float z, float r, float g, float b, float a, float u, float v, int light) {
        vertexConsumer.vertex(last, x, y, z).color(r, g, b, a).texture(u, v).light(light).next();
    }

    public static float distSqr(float... a) {
        float d = 0.0F;
        for (float f : a) {
            d += f * f;
        }
        return d;
    }

    public static float distance(float... a) {
        return MathHelper.sqrt(distSqr(a));
    }

    public static void renderBlockAtPosition(WorldRenderContext context, Vec3d pos, Identifier texture, float alpha) {
        renderBlockAtPosition(context.matrixStack(), context.camera(), pos, texture, alpha, GameRenderer::getPositionColorTexShader);
    }

    public static void renderBlockAtPosition(MatrixStack matrixStack, Camera camera, Vec3d pos, Identifier texture, float alpha) {
        renderBlockAtPosition(matrixStack, camera, pos, texture, alpha, GameRenderer::getPositionColorTexShader);
    }

    public static void renderBlock(MatrixStack matrixStack, VertexConsumer vertexConsumer){

        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        renderSide(matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        renderSide(matrix4f, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        renderSide(matrix4f, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
        renderSide(matrix4f, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F);
        renderSide(matrix4f, vertexConsumer, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
    }

    private static void renderSide(Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4) {
        vertices.vertex(model, x1, y1, z1).next();
        vertices.vertex(model, x2, y1, z2).next();
        vertices.vertex(model, x2, y2, z3).next();
        vertices.vertex(model, x1, y2, z4).next();
    }

    /**
     * Renders a Block at a pos
     */
    public static void renderBlockAtPosition(MatrixStack matrixStack, Camera camera, Vec3d pos, Identifier texture, float alpha, Supplier<Shader> shader) {
        matrixStack.push();
        Vec3d transformedPos = pos.subtract(camera.getPos());
        matrixStack.translate(transformedPos.x, transformedPos.y, transformedPos.z);
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);

        Color color = new Color(255, 255, 255);
        int intColor = color.getRGB();

        for (Direction direction : Direction.values()) {
            float x1 = direction == Direction.WEST || direction == Direction.DOWN || direction == Direction.NORTH ? 1 : 0;
            float x2 = direction == Direction.EAST || direction == Direction.UP || direction == Direction.SOUTH ? 1 : 0;
            float y1 = direction == Direction.DOWN || direction == Direction.NORTH || direction == Direction.WEST ? 1 : 0;
            float y2 = direction == Direction.UP || direction == Direction.SOUTH || direction == Direction.EAST ? 1 : 0;
            float z1 = direction == Direction.NORTH || direction == Direction.UP || direction == Direction.WEST ? 1 : 0;
            float z2 = direction == Direction.SOUTH || direction == Direction.DOWN || direction == Direction.EAST ? 1 : 0;

            buffer.vertex(positionMatrix, x1, y1, z1).color(intColor).texture(0, 1).next();
            buffer.vertex(positionMatrix, x1, y2, z2).color(intColor).texture(0, 0).next();
            buffer.vertex(positionMatrix, x2, y2, z2).color(intColor).texture(1, 0).next();
            buffer.vertex(positionMatrix, x2, y1, z1).color(intColor).texture(1, 1).next();
        }

        RenderSystem.setShader(shader);
        RenderSystem.setShaderTexture(0, texture);
        tessellator.draw();

        matrixStack.pop();
    }
}
