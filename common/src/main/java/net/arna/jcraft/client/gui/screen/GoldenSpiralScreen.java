package net.arna.jcraft.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GoldenSpiralScreen extends Screen {

    public static final int ITERATIONS = 9;
    public static final int ARC_SEGMENTS = 32;

    protected final List<Square> squares = new LinkedList<>();
    protected final List<Vec2> spiralPoints = new ArrayList<>();
    protected final List<Vec2> playerPoints = new ArrayList<>();
    protected boolean tracing = false;

    public GoldenSpiralScreen() {
        super(Component.literal("Golden Spiral Screen"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void calculatePoints(final float startX, final float startY, final float scale) {
        squares.clear();
        spiralPoints.clear();
        float x = startX;
        float y = startY;
        int f0 = 0;
        int f1 = 1;
        for (int i = 0; i < ITERATIONS; i++) {
            final int fNext = f0 + f1;
            f0 = f1;
            f1 = fNext;

            final float size = f1 * scale;

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
            squares.add(new Square((int)x, (int)y, (int)size));

            double arcX = x;
            double arcY = y;
            int arcStartAngle = 0;
            switch (i % 4) {
                case 0 -> arcStartAngle = 270;
                case 1 -> arcY += size;
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
            for (int s = 0; s < ARC_SEGMENTS; s++) {
                final double currentAngle = arcStartAngle + (s * (90d / ARC_SEGMENTS));
                spiralPoints.add(new Vec2(
                        (float)(arcX + size * Math.cos(Math.toRadians(currentAngle))),
                        (float)(arcY - size * Math.sin(Math.toRadians(currentAngle)))
                ));
            }
        }
        spiralPoints.add(new Vec2(x + f1 * scale, y));
        spiralPoints.add(new Vec2(x + f1 * scale, y-1));
    }

    @Override
    public void resize(final @NonNull Minecraft minecraft, final int width, final int height) {
        super.resize(minecraft, width, height);
        calculatePoints(width / 3f, height / 2.8f, 3f);
        tracing = false;
        playerPoints.clear();
    }

    // draw spiral in this method
    @Override
    public void render(final @NonNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // takes care of widgets and such

        final float scale = 3f;
        float x = this.width / 3f;
        float y = this.height / 2.8f;

        final int gridColor = Color.getHSBColor(0, 0, 0.3f).getRGB();
        final int spiralColor = 0xFFD48F38;
        final int playerColor = 0xFF38D48F;

        if (squares.isEmpty()) {
            calculatePoints(x, y, scale);
        }

        for (final Square square : squares) {
            drawBorderedSquare(guiGraphics, square.x, square.y, square.length, gridColor);
        }

        final PoseStack.Pose pose = guiGraphics.pose().last();
        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.lineStrip());
        Vec2 currentPoint = spiralPoints.get(0);
        Vec2 nextPoint;
        for (int i = 1; i < spiralPoints.size(); i++) {
            nextPoint = spiralPoints.get(i);
            final Vec2 diff = nextPoint.add(currentPoint.negated());
            consumer.vertex(pose.pose(), currentPoint.x, currentPoint.y, 0f)
                    .color(spiralColor)
                    .normal(pose.normal(), diff.x, diff.y, 0f)
                    .endVertex();
            currentPoint = nextPoint;
        }
        if (!playerPoints.isEmpty() && !tracing) {
            consumer = guiGraphics.bufferSource().getBuffer(RenderType.lineStrip());
            currentPoint = playerPoints.get(0);
            for (int i = 1; i < playerPoints.size(); i++) {
                nextPoint = playerPoints.get(i);
                final Vec2 diff = nextPoint.add(currentPoint.negated());
                consumer.vertex(pose.pose(), currentPoint.x, currentPoint.y, 0f)
                        .color(playerColor)
                        .normal(pose.normal(), diff.x, diff.y, 0f)
                        .endVertex();
                currentPoint = nextPoint;
            }
        }
    }

    protected void drawBorderedSquare(final @NonNull GuiGraphics graphics, final int x, final int y, final int length, final int color) {
        graphics.hLine(x, x + length, y, color);
        graphics.hLine(x, x + length, y + length, color);
        graphics.vLine(x, y, y + length, color);
        graphics.vLine(x + length, y, y + length, color);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            playerPoints.clear();
            tracing = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        if (button == 0 && tracing) {
            playerPoints.add(new Vec2((float)(mouseX + dragX), (float)(mouseY + dragY)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            tracing = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected record Square(int x, int y, int length) {
        // intentionally left empty
    }

}
