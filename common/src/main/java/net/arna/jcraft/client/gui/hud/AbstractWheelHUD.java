package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * Shared rendering + hit-testing for radial-menu HUDs. Subclasses provide slot count,
 * per-slot color/label/active state, and optional center content (e.g. a player preview).
 * <p>
 * The wheel renders in a fixed GUI scale (default 2.0) so it's the same physical size
 * regardless of the player's MC GUI scale setting.
 */ // FIXME yeah that's actually bad that it doesn't scale
public abstract class AbstractWheelHUD {

    protected static final float FIXED_GUI_SCALE = 2f;

    protected final int slots;
    protected final int outerRadius;
    protected final int innerRadius;
    protected final int centerRadius;

    @Setter
    @Getter
    protected int hoveredSlot = -1;

    protected AbstractWheelHUD(int slots, int outerRadius, int innerRadius, int centerRadius) {
        this.slots = slots;
        this.outerRadius = outerRadius;
        this.innerRadius = innerRadius;
        this.centerRadius = centerRadius;
    }

    // Subclass hooks
    /** RGBA used to fill segment. Default depends on hovered/active flags. */
    protected float[] segmentColor(int index, boolean hovered, boolean active) {
        float r = active ? 0.25f : (hovered ? 0.30f : 0.18f);
        float g = active ? 0.60f : (hovered ? 0.40f : 0.18f);
        float b = active ? 0.25f : (hovered ? 0.75f : 0.35f);
        return new float[]{ r, g, b, 0.80f };
    }

    /** Whether slot is "currently selected" (highlighted differently). */
    protected boolean isActiveSlot(int index) { return false; }

    /** First line of label text (e.g. slot number). Return null to skip. */
    protected String labelTop(int index) { return String.valueOf(index + 1); }

    /** Second line of label text (e.g. pose name). Return null to skip. */
    protected String labelBottom(int index) { return null; }

    // FIXME ARGB or RGBA, decide on one

    /** ARGB for the bottom label. Default highlights hovered/configured slots. */
    protected int labelBottomColor(int index, boolean hovered) {
        return hovered ? 0xFFFFDD44 : (labelBottom(index) != null ? 0xFFFFFFFF : 0xFF888888);
    }

    /** Optional: draw something inside the center circle (entity preview, item icon, etc.). */
    protected void renderCenter(GuiGraphics gui, int cx, int cy, float tickDelta) {}

    /**
     * Main render entry point. Drawing order:
     * outer bg -> segments -> separators -> ring border -> labels -> center bg -> center content -> center border.
     */
    public void render(GuiGraphics gui, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double currentScale = mc.getWindow().getGuiScale();
        double ratio = FIXED_GUI_SCALE / currentScale;

        gui.pose().pushPose();
        gui.pose().scale((float) ratio, (float) ratio, 1f);

        int scaledW = (int) (mc.getWindow().getWidth() / FIXED_GUI_SCALE);
        int scaledH = (int) (mc.getWindow().getHeight() / FIXED_GUI_SCALE);
        int cx = scaledW / 2;
        int cy = scaledH / 2;

        updateHover(cx, cy, ratio, mc);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Matrix4f matrix = gui.pose().last().pose();

        drawFilledCircle(matrix, cx, cy, outerRadius, 0.06f, 0.06f, 0.10f, 0.85f);

        float segAngle = 360f / slots;
        for (int i = 0; i < slots; i++) {
            float start = -90f + i * segAngle;
            float end = start + segAngle;
            float[] col = segmentColor(i, i == hoveredSlot, isActiveSlot(i));
            drawSegment(matrix, cx, cy, start, end, col[0], col[1], col[2], col[3]);
        }

        for (int i = 0; i < slots; i++) {
            drawSeparator(matrix, cx, cy, -90f + i * segAngle);
        }

        drawCircleBorder(matrix, cx, cy, outerRadius, 0.20f, 0.22f, 0.30f, 1f);

        Font font = mc.font;
        for (int i = 0; i < slots; i++) {
            float midAngle = -90f + i * segAngle + segAngle / 2f;
            int labelR = (innerRadius + outerRadius) / 2;
            int lx = cx + (int) (labelR * Math.cos(Math.toRadians(midAngle)));
            int ly = cy + (int) (labelR * Math.sin(Math.toRadians(midAngle)));

            boolean hovered = (i == hoveredSlot);
            String top = labelTop(i);
            String bot = labelBottom(i);
            if (top != null) gui.drawCenteredString(font, top, lx, ly - 10, 0xFFAAAAAA);
            if (bot != null) gui.drawCenteredString(font, bot, lx, ly, labelBottomColor(i, hovered));
        }

        drawFilledCircle(matrix, cx, cy, centerRadius, 0.07f, 0.07f, 0.11f, 0.92f);
        renderCenter(gui, cx, cy, tickDelta);
        drawCircleBorder(matrix, cx, cy, centerRadius, 0.28f, 0.30f, 0.42f, 1f);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        gui.pose().popPose();
    }

    // Hit testing
    protected void updateHover(int cx, int cy, double ratio, Minecraft mc) {
        double rawMX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double rawMY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        double mx = rawMX / ratio;
        double my = rawMY / ratio;

        double dx = mx - cx;
        double dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < innerRadius || dist > outerRadius) {
            hoveredSlot = -1;
            return;
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;
        if (angle >= 360) angle -= 360;

        float segAngle = 360f / slots;
        int slot = (int) Math.floor(angle / segAngle);
        hoveredSlot = Math.max(0, Math.min(slot, slots - 1));
    }

    // Geometry helpers
    protected void drawSegment(Matrix4f m, int cx, int cy, float startDeg, float endDeg,
                               float r, float g, float b, float a) {
        int steps = 24;
        float step = (endDeg - startDeg) / steps;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(startDeg + i * step);
            float a2 = (float) Math.toRadians(startDeg + (i + 1) * step);

            float x1i = cx + innerRadius * (float) Math.cos(a1);
            float y1i = cy + innerRadius * (float) Math.sin(a1);
            float x2i = cx + innerRadius * (float) Math.cos(a2);
            float y2i = cy + innerRadius * (float) Math.sin(a2);

            float x1o = cx + outerRadius * (float) Math.cos(a1);
            float y1o = cy + outerRadius * (float) Math.sin(a1);
            float x2o = cx + outerRadius * (float) Math.cos(a2);
            float y2o = cy + outerRadius * (float) Math.sin(a2);

            buf.vertex(m, x1i, y1i, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x1o, y1o, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x2o, y2o, 0).color(r, g, b, a).endVertex();

            buf.vertex(m, x1i, y1i, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x2o, y2o, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x2i, y2i, 0).color(r, g, b, a).endVertex();
        }
        tess.end();
    }

    protected static void drawFilledCircle(Matrix4f m, int cx, int cy, int radius, float r, float g, float b, float a) {
        int steps = 64;
        float step = 360f / steps;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(i * step);
            float a2 = (float) Math.toRadians((i + 1) * step);
            buf.vertex(m, cx, cy, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, cx + radius * (float) Math.cos(a1), cy + radius * (float) Math.sin(a1), 0).color(r, g, b, a).endVertex();
            buf.vertex(m, cx + radius * (float) Math.cos(a2), cy + radius * (float) Math.sin(a2), 0).color(r, g, b, a).endVertex();
        }
        tess.end();
    }

    protected static void drawCircleBorder(Matrix4f m, int cx, int cy, int radius, float r, float g, float b, float a) {
        final int steps = 64;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float step = 360f / steps;
        float thickness = 1.5f;
        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(i * step);
            float a2 = (float) Math.toRadians((i + 1) * step);
            float x1i = cx + (radius - thickness) * (float) Math.cos(a1);
            float y1i = cy + (radius - thickness) * (float) Math.sin(a1);
            float x1o = cx + radius * (float) Math.cos(a1);
            float y1o = cy + radius * (float) Math.sin(a1);
            float x2i = cx + (radius - thickness) * (float) Math.cos(a2);
            float y2i = cy + (radius - thickness) * (float) Math.sin(a2);
            float x2o = cx + radius * (float) Math.cos(a2);
            float y2o = cy + radius * (float) Math.sin(a2);
            // TODO maybe use RenderUtils
            buf.vertex(m, x1i, y1i, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x1o, y1o, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x2o, y2o, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x1i, y1i, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x2o, y2o, 0).color(r, g, b, a).endVertex();
            buf.vertex(m, x2i, y2i, 0).color(r, g, b, a).endVertex();
        }
        tess.end();
    }

    protected void drawSeparator(Matrix4f m, int cx, int cy, float angleDeg) {
        float rad = (float) Math.toRadians(angleDeg);
        float cosA = (float) Math.cos(rad);
        float sinA = (float) Math.sin(rad);

        float x1 = cx + innerRadius * cosA;
        float y1 = cy + innerRadius * sinA;
        float x2 = cx + outerRadius * cosA;
        float y2 = cy + outerRadius * sinA;

        float thickness = 0.35f;
        float nx = -sinA * thickness;
        float ny = cosA * thickness;

        float r = 0.45f, g = 0.50f, b = 0.65f, a = 0.80f;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        buf.vertex(m, x1 - nx, y1 - ny, 0).color(r, g, b, a).endVertex();
        buf.vertex(m, x1 + nx, y1 + ny, 0).color(r, g, b, a).endVertex();
        buf.vertex(m, x2 + nx, y2 + ny, 0).color(r, g, b, a).endVertex();

        buf.vertex(m, x1 - nx, y1 - ny, 0).color(r, g, b, a).endVertex();
        buf.vertex(m, x2 + nx, y2 + ny, 0).color(r, g, b, a).endVertex();
        buf.vertex(m, x2 - nx, y2 - ny, 0).color(r, g, b, a).endVertex();

        tess.end();
    }
}
