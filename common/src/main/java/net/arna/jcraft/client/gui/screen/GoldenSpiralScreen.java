package net.arna.jcraft.client.gui.screen;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.NonNull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

import java.awt.Color;

public class GoldenSpiralScreen extends Screen {

    public GoldenSpiralScreen() {
        super(Component.literal("Golden Spiral Screen"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    // draw spiral in this method
    @Override
    public void render(final @NonNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // takes care of widgets and such

        final double scale = 3.0;
        double x = this.width / 3d;
        double y = this.height / 2.8;

        int f0 = 0;
        int f1 = 1;
        final int iterations = 9;

        final int gridColor = Color.getHSBColor(0, 0, 0.3f).getRGB();
        final int spiralColor = 0xFFD48F38;

        for (int i = 0; i < iterations; i++) {

            final int fNext = f0 + f1;
            f0 = f1;
            f1 = fNext;

            final double size = f1 * scale;
            double arcStartAngle = 0;

            // (x,y) is the upper left corner of the square
            switch (i % 4) {
                case 0 -> {
                    x += f0 * scale;
                    y -= (f1-f0) * scale;
                }
                case 1 -> {
                    y -= size;
                    x -= (f1-f0) * scale;
                }
                case 2 -> x -= size;
                case 3 -> y += f0 * scale;
            }
            drawBorderedRect(guiGraphics, (int)x, (int)y, (int)size, (int)size, gridColor);

            double arcX = x;
            double arcY = y;
            switch (i % 4) {
                case 0 -> arcStartAngle = 270;
                case 1 -> {
                    arcY += size;
                    arcStartAngle = 0;
                }
                case 2 -> {
                    arcX += size;
                    arcY += size;
                    arcStartAngle = 90;
                }
                case 3 -> {
                    arcX += size;
                    arcStartAngle = 180;
                }
            }
            drawMinecraftArc(guiGraphics, arcX, arcY, size, arcStartAngle, spiralColor);
        }
    }

    private void drawMinecraftArc(final @NonNull GuiGraphics guiGraphics, final double centerX, final double centerY, final double radius, final double startAngle, final int color) {
        final int segments = 32;
        final Matrix4f matrix4f = guiGraphics.pose().last().pose();
        final VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        for (int i = 0; i <= segments; i++) {
            double currentAngle = startAngle + (i * (90d / segments));
            consumer.vertex(matrix4f, (float)(centerX + radius * Math.cos(Math.toRadians(currentAngle))),
                            (float)(centerY - radius * Math.sin(Math.toRadians(currentAngle))), 0)
                    .color(color).endVertex();
        }
        guiGraphics.bufferSource().endBatch(RenderType.gui());
        guiGraphics.flush();
    }

    private void drawBorderedRect(final @NonNull GuiGraphics graphics, final int x, final int y, final int width, final int height, final int color) {
        graphics.hLine(x, x + width, y, color);
        graphics.hLine(x, x + width, y + height, color);
        graphics.vLine(x, y, y + height, color);
        graphics.vLine(x + width, y, y + height, color);
    }

}
