package net.arna.jcraft.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.networking.NetworkManager;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.network.c2s.GoldenRatioPacket;
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
    public static final int TOLERANCE_SQR = 200;

    protected final List<Square> squares = new LinkedList<>();
    protected final List<Vec2> spiralPoints = new ArrayList<>();
    protected final List<Vec2> playerPoints = new ArrayList<>();
    protected final List<Float> playerSquaredDeviations = new ArrayList<>();
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
            // (x,y) is now the upper left corner of the square
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

    public void calculatePoints() {
        calculatePoints(width / 3f, height / 2.8f, 3f);
    }

    public void cancelTracing() {
        tracing = false;
        playerPoints.clear();
        playerSquaredDeviations.clear();
    }

    public float findShortestDeviationSqr(final @NonNull Vec2 point) {
        if (spiralPoints.isEmpty()) {
            calculatePoints();
        }
        float shortest = Float.MAX_VALUE;
        for (final Vec2 spiralPoint : spiralPoints) {
            float current = point.distanceToSqr(spiralPoint);
            if (current < shortest) {
                shortest = current;
            }
        }
        return shortest;
    }

    public float calculateStandardDeviation() {
        float expectedValue = (float)playerSquaredDeviations.stream()
                .mapToDouble(f -> Math.sqrt((double)f))
                .average().getAsDouble();
        return (float)Math.sqrt(playerSquaredDeviations.stream()
                .mapToDouble(f -> Math.pow(expectedValue - Math.sqrt(f), 2))
                .sum());
    }

    @Override
    public void resize(final @NonNull Minecraft minecraft, final int width, final int height) {
        super.resize(minecraft, width, height);
        calculatePoints();
        cancelTracing();
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
        Vec2 currentPoint = spiralPoints.get(0);
        Vec2 nextPoint = spiralPoints.get(spiralPoints.size() - 2);
        // draw start or goal square
        if (!tracing) {
            guiGraphics.fill((int)currentPoint.x - 2, (int)currentPoint.y - 2,
                    (int)currentPoint.x + 2, (int)currentPoint.y + 2, 0xFFFF0000);
        }
        else {
            guiGraphics.fill((int)nextPoint.x - 4, (int)nextPoint.y - 4,
                    (int)nextPoint.x + 4, (int)nextPoint.y + 4, 0xFFFF0000);
        }
        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.lineStrip());
        // draw the spiral
        for (int i = 1; i < spiralPoints.size(); i++) {
            nextPoint = spiralPoints.get(i);
            final Vec2 diff = nextPoint.add(currentPoint.negated());
            consumer.vertex(pose.pose(), currentPoint.x, currentPoint.y, 0f)
                    .color(spiralColor)
                    .normal(pose.normal(), diff.x, diff.y, 0f)
                    .endVertex();
            currentPoint = nextPoint;
        }
        if (!playerPoints.isEmpty()) {
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
            final Vec2 startPoint = spiralPoints.get(0);
            if (mouseX >= startPoint.x - 4 && mouseX <= startPoint.x + 4 &&
                    mouseY >= startPoint.y - 4 && mouseY <= startPoint.y + 4) {
                cancelTracing();
                tracing = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        if (button == 0 && tracing) {
            final Vec2 playerPoint = new Vec2((float)(mouseX + dragX), (float)(mouseY + dragY));
            playerPoints.add(playerPoint);
            float shortestDeviationSqr = findShortestDeviationSqr(playerPoint);
            if (shortestDeviationSqr > TOLERANCE_SQR) {
                cancelTracing();
            }
            playerSquaredDeviations.add(shortestDeviationSqr);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            if (tracing) {
                final Vec2 endPoint = spiralPoints.get(spiralPoints.size() - 2); // -2 because the last point is just there for rendering purposes
                if (mouseX >= endPoint.x - 2 && mouseX <= endPoint.x + 2 &&
                        mouseY >= endPoint.y - 2 && mouseY <= endPoint.y + 2) {
                    final float score = calculateStandardDeviation();
                    NetworkManager.sendToServer(JPacketRegistry.C2S_GOLDEN_RATIO, GoldenRatioPacket.write(score));
                    JCraft.LOGGER.info("You got {} standard deviation!", score);
                    Minecraft.getInstance().setScreen(null);
                }
                else {
                    JCraft.LOGGER.info("You failed the Golden Spiral tracing!");
                }
            }
            tracing = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected record Square(int x, int y, int length) {
        // intentionally left empty
    }

}
