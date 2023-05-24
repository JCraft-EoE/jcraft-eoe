package net.arna.jcraft.common.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class RenderUtils {

    public static void renderBlockAtPosition(WorldRenderContext context, Vec3d pos, Identifier texture, float alpha) {
        renderBlockAtPosition(context.matrixStack(), context.camera(), pos, texture, alpha);
    }

    /**
     * Renders a Block at a pos
     */
    public static void renderBlockAtPosition(MatrixStack matrixStack, Camera camera, Vec3d pos, Identifier texture, float alpha) {
        matrixStack.push();
        Vec3d transformedPos = pos.subtract(camera.getPos());
        matrixStack.translate(transformedPos.x, transformedPos.y, transformedPos.z);
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);

        Color color = new Color(255, 255, 255, alpha);
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

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, texture);
        tessellator.draw();

        matrixStack.pop();
    }
}
