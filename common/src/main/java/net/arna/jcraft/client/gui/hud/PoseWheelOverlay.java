package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.gui.screen.PoseWheelConfigScreen;
import net.arna.jcraft.client.pose.PoseDefinition;
import net.arna.jcraft.client.pose.PoseWheelConfig;
import net.arna.jcraft.client.pose.PoseWheelState;
import net.arna.jcraft.common.util.IJCraftAnimatedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders the 9-segment pose wheel overlay when PoseWheelState.isOpen().
 * Also draws the bottom-right config button.
 */
public class PoseWheelOverlay {

    private static final int SLOTS = 9;
    private static final int OUTER_RADIUS = 120;
    private static final int INNER_RADIUS = 44;
    private static final int CENTER_CIRCLE_RADIUS = 38;
    private static final int PLAYER_PREVIEW_SCALE = 30;

    // Config button (bottom-right)
    private static final int BTN_W = 80;
    private static final int BTN_H = 16;
    private static final int BTN_MARGIN = 6;

    private static final double FIXED_GUI_SCALE = 2.0;
    @Getter
    private static float tickDelta;

    // Preview animation for center square
    @Nullable private static KeyframeAnimationPlayer previewAnimPlayer = null;
    private static int lastPreviewSlot = -2;
    private static long previewLastTickMs = 0L;

    public static void render(GuiGraphics gui, float tickDelta) {
        if (!PoseWheelState.isOpen()) return;

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

        // Outer background circle
        drawFilledCircle(matrix, cx, cy, OUTER_RADIUS, 0.06f, 0.06f, 0.10f, 0.85f);

        // Segments
        float segAngle = 360f / SLOTS;
        for (int i = 0; i < SLOTS; i++) {
            float start = -90f + i * segAngle;
            float end = start + segAngle;
            boolean hovered = (i == PoseWheelState.getHoveredSlot());
            boolean active = isActiveSlot(i);
            drawSegment(matrix, cx, cy, start, end, hovered, active);
        }

        // Separator lines between segments (drawn after segments so they sit on top)
        for (int i = 0; i < SLOTS; i++) {
            float angle = -90f + i * segAngle;
            drawSeparator(matrix, cx, cy, angle);
        }

        // Outer ring border
        drawCircleBorder(matrix, cx, cy, OUTER_RADIUS, 0.20f, 0.22f, 0.30f, 1f);

        // Slot labels (index + pose name)
        Font font = mc.font;
        for (int i = 0; i < SLOTS; i++) {
            float midAngle = -90f + i * segAngle + segAngle / 2f;
            int labelR = (INNER_RADIUS + OUTER_RADIUS) / 2;
            int lx = cx + (int) (labelR * Math.cos(Math.toRadians(midAngle)));
            int ly = cy + (int) (labelR * Math.sin(Math.toRadians(midAngle)));

            PoseDefinition def = PoseWheelConfig.getPoseAt(i);
            String line1 = (i + 1) + "";
            String line2 = def != null ? trimName(def.displayName()) : "—";

            boolean hovered = (i == PoseWheelState.getHoveredSlot());
            int color = hovered ? 0xFFFFDD44 : (def != null ? 0xFFFFFFFF : 0xFF888888);

            gui.drawCenteredString(font, line1, lx, ly - 10, 0xFFAAAAAA);
            gui.drawCenteredString(font, line2, lx, ly, color);
        }

        // Center circle background
        drawFilledCircle(consumer, matrix, cx, cy, CENTER_CIRCLE_RADIUS, 0.07f, 0.07f, 0.11f, 0.92f);

        // The entity preview internally calls gui.flush(), which flushes everything queued so
        // far (wheel geometry + labels), then renders the player on top. Anything we queue
        // AFTER this point ends up drawn on top of the player at end-of-frame flush.
        renderPlayerPreview(gui, cx, cy, mc.player, tickDelta);

        // Re-fetch consumer — gui.flush() inside the preview ended the previous batch.
        consumer = buffers.getBuffer(RenderType.gui());
        drawCircleBorder(consumer, matrix, cx, cy, CENTER_CIRCLE_RADIUS, 0.28f, 0.30f, 0.42f, 1f);

        int btnX = scaledW - BTN_W - BTN_MARGIN;
        int btnY = scaledH - BTN_H - BTN_MARGIN;
        renderConfigButton(gui, font, btnX, btnY, mc, ratio);

        gui.pose().popPose();
    }

    // Hover detection
    private static void updateHover(int cx, int cy, double ratio, Minecraft mc) {
        double rawMX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double rawMY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        double mx = rawMX / ratio;
        double my = rawMY / ratio;

        double dx = mx - cx;
        double dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < INNER_RADIUS || dist > OUTER_RADIUS) {
            PoseWheelState.setHoveredSlot(-1);
            return;
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;
        if (angle >= 360) angle -= 360;

        float segAngle = 360f / SLOTS;
        int slot = (int) Math.floor(angle / segAngle);
        PoseWheelState.setHoveredSlot(Math.max(0, Math.min(slot, SLOTS - 1)));
    }

    /**
     * Click handling (called from tickClient):
     * Returns true and opens the config screen if the cursor is over the config button.
     */
    public static boolean tryClickConfigButton(Minecraft mc) {
        double ratio = FIXED_GUI_SCALE / mc.getWindow().getGuiScale();
        double rawMX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double rawMY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        double mx = rawMX / ratio;
        double my = rawMY / ratio;

        int scaledW = (int) (mc.getWindow().getWidth() / FIXED_GUI_SCALE);
        int scaledH = (int) (mc.getWindow().getHeight() / FIXED_GUI_SCALE);
        int btnX = scaledW - BTN_W - BTN_MARGIN;
        int btnY = scaledH - BTN_H - BTN_MARGIN;

        if (mx >= btnX && mx <= btnX + BTN_W && my >= btnY && my <= btnY + BTN_H) {
            mc.setScreen(new PoseWheelConfigScreen());
            PoseWheelState.close();
            return true;
        }
        return false;
    }

    private static void renderPlayerPreview(GuiGraphics gui, int cx, int cy, Player player, float tickDelta) {
        PoseWheelOverlay.tickDelta = tickDelta;
        Quaternionf poseRot = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(0f);

        // Prefer the idle frame — that's what the player actually looks like once posed.
        int hovered = PoseWheelState.getHoveredSlot();
        PoseDefinition def = hovered >= 0 ? PoseWheelConfig.getPoseAt(hovered) : null;
        String previewAnimId = null;
        if (def != null) {
            previewAnimId = def.idleAnimId() != null ? def.idleAnimId() : def.windupAnimId();
        } else if (PoseWheelState.getActivePoseAnimId() != null) {
            previewAnimId = PoseWheelState.getActivePoseAnimId();
        }

        if (previewAnimId != null) {
            if (hovered != lastPreviewSlot) {
                lastPreviewSlot = hovered;
                KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(JCraft.id(previewAnimId));
                previewAnimPlayer = anim != null ? new KeyframeAnimationPlayer(anim) : null;
                previewLastTickMs = System.currentTimeMillis();
                if (previewAnimPlayer != null) {
                    // skip the fade-in so we land on a visible frame
                    for (int i = 0; i < 4; i++) previewAnimPlayer.tick();
                }
            }
        } else {
            previewAnimPlayer = null;
            lastPreviewSlot = -2;
        }

        // Tick at 20 TPS regardless of framerate.
        if (previewAnimPlayer != null) {
            long now = System.currentTimeMillis();
            if (previewLastTickMs == 0L) previewLastTickMs = now;
            while (now - previewLastTickMs >= 50L) {
                previewAnimPlayer.tick();
                previewLastTickMs += 50L;
            }
        }

        ModifierLayer<IAnimation> layer = ((IJCraftAnimatedPlayer) player).jcraft_getModAnimation();
        IAnimation savedAnim = layer.getAnimation();

        // Stash and override entity facing so the player faces the camera.
        float sBody = player.yBodyRot, sBodyO = player.yBodyRotO;
        float sYaw = player.getYRot(), sYawO = player.yRotO;
        float sPitch = player.getXRot(), sPitchO = player.xRotO;
        float sHead = player.yHeadRot, sHeadO = player.yHeadRotO;

        player.yBodyRot = 180f; player.yBodyRotO = 180f;
        player.setYRot(180f);   player.yRotO = 180f;
        player.setXRot(0f);     player.xRotO = 0f;
        player.yHeadRot = 180f; player.yHeadRotO = 180f;

        if (previewAnimPlayer != null) layer.setAnimation(previewAnimPlayer);
        renderEntityInCenter(gui, cx, cy + CENTER_CIRCLE_RADIUS - 6, PLAYER_PREVIEW_SCALE, poseRot, camera, player);
        layer.setAnimation(savedAnim);

        player.yBodyRot = sBody;  player.yBodyRotO = sBodyO;
        player.setYRot(sYaw);     player.yRotO = sYawO;
        player.setXRot(sPitch);   player.xRotO = sPitchO;
        player.yHeadRot = sHead;  player.yHeadRotO = sHeadO;
    }

    private static void renderEntityInCenter(GuiGraphics gui, int x, int y, int scale,
                                              Quaternionf pose, Quaternionf cameraOrientation, LivingEntity entity) {
        gui.pose().pushPose();
        gui.pose().translate(x, y, 50.0);
        gui.pose().mulPoseMatrix(new Matrix4f().scaling(scale, scale, -scale));
        gui.pose().mulPose(pose);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        cameraOrientation.conjugate();
        dispatcher.overrideCameraOrientation(cameraOrientation);
        dispatcher.setRenderShadow(false);

        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F,
                gui.pose(), gui.bufferSource(), 15728880));
        gui.flush();
        dispatcher.setRenderShadow(true);
        gui.pose().popPose();
        Lighting.setupFor3DItems();
    }

    private static void renderConfigButton(GuiGraphics gui, Font font, int bx, int by, Minecraft mc, double ratio) {
        double rawMX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double rawMY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        double mx = rawMX / ratio;
        double my = rawMY / ratio;
        boolean btnHover = mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H;

        int bg = btnHover ? 0xCC3355AA : 0xCC1A2233;
        gui.fill(bx, by, bx + BTN_W, by + BTN_H, bg);
        gui.renderOutline(bx, by, BTN_W, BTN_H, 0xFF4466BB);
        gui.drawCenteredString(font, "Configure Poses", bx + BTN_W / 2, by + (BTN_H - 8) / 2, 0xFFCCDDFF);
    }

    private static String trimName(String name) {
        if (name.length() > 12) return name.substring(0, 11) + "…";
        return name;
    }

    private static boolean isActiveSlot(int index) {
        PoseDefinition def = PoseWheelConfig.getPoseAt(index);
        if (def == null) return false;
        String active = PoseWheelState.getActivePoseAnimId();
        if (active == null) return false;
        return active.equals(def.windupAnimId()) ||
                (def.idleAnimId() != null && active.equals(def.idleAnimId()));
    }

    // All shapes are submitted through gui.bufferSource() + RenderType.gui(), the same path
    // gui.fill() uses. RenderType.gui() handles depth-test / blend / shader state for us.
    private static void drawSegment(VertexConsumer c, Matrix4f m, int cx, int cy,
                                    float startDeg, float endDeg, boolean hovered, boolean active) {
        int steps = 24;
        float step = (endDeg - startDeg) / steps;

        float r = active ? 0.25f : (hovered ? 0.30f : 0.18f);
        float g = active ? 0.60f : (hovered ? 0.40f : 0.18f);
        float b = active ? 0.25f : (hovered ? 0.75f : 0.35f);
        float a = 0.80f;

        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(startDeg + i * step);
            float a2 = (float) Math.toRadians(startDeg + (i + 1) * step);

            float x1i = cx + INNER_RADIUS * (float) Math.cos(a1);
            float y1i = cy + INNER_RADIUS * (float) Math.sin(a1);
            float x2i = cx + INNER_RADIUS * (float) Math.cos(a2);
            float y2i = cy + INNER_RADIUS * (float) Math.sin(a2);

            float x1o = cx + OUTER_RADIUS * (float) Math.cos(a1);
            float y1o = cy + OUTER_RADIUS * (float) Math.sin(a1);
            float x2o = cx + OUTER_RADIUS * (float) Math.cos(a2);
            float y2o = cy + OUTER_RADIUS * (float) Math.sin(a2);

            quad(c, m, x1i, y1i, x1o, y1o, x2o, y2o, x2i, y2i, r, g, b, a);
        }
    }

    private static void drawFilledCircle(VertexConsumer c, Matrix4f m, int cx, int cy, int radius,
                                         float r, float g, float b, float a) {
        int steps = 64;
        float step = 360f / steps;
        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(i * step);
            float a2 = (float) Math.toRadians((i + 1) * step);
            // Triangle fan emitted as two triangles per slice for RenderType.gui (which uses QUADS).
            float x1 = cx + radius * (float) Math.cos(a1);
            float y1 = cy + radius * (float) Math.sin(a1);
            float x2 = cx + radius * (float) Math.cos(a2);
            float y2 = cy + radius * (float) Math.sin(a2);
            // Degenerate quad: two verts share the center, forming a triangle slice.
            c.vertex(m, cx, cy, 0).color(r, g, b, a).endVertex();
            c.vertex(m, x1, y1, 0).color(r, g, b, a).endVertex();
            c.vertex(m, x2, y2, 0).color(r, g, b, a).endVertex();
            c.vertex(m, cx, cy, 0).color(r, g, b, a).endVertex();
        }
    }

    private static void drawCircleBorder(VertexConsumer c, Matrix4f m, int cx, int cy, int radius,
                                         float r, float g, float b, float a) {
        int steps = 128;
        float step = 360f / steps;
        float thickness = 1.5f;
        for (int i = 0; i < steps; i++) {
            float a1 = (float) Math.toRadians(i * step);
            float a2 = (float) Math.toRadians((i + 1) * step);
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            quad(c, m,
                cx + (radius - thickness) * cos1, cy + (radius - thickness) * sin1,
                cx + radius * cos1,               cy + radius * sin1,
                cx + radius * cos2,               cy + radius * sin2,
                cx + (radius - thickness) * cos2, cy + (radius - thickness) * sin2,
                r, g, b, a);
        }
    }

    private static void drawSeparator(VertexConsumer c, Matrix4f m, int cx, int cy, float angleDeg) {
        float rad = (float) Math.toRadians(angleDeg);
        float cosA = (float) Math.cos(rad);
        float sinA = (float) Math.sin(rad);

        float x1 = cx + INNER_RADIUS * cosA;
        float y1 = cy + INNER_RADIUS * sinA;
        float x2 = cx + OUTER_RADIUS * cosA;
        float y2 = cy + OUTER_RADIUS * sinA;

        float thickness = 0.5f;
        float nx = -sinA * thickness;
        float ny = cosA * thickness;

        quad(c, m,
            x1 - nx, y1 - ny,
            x1 + nx, y1 + ny,
            x2 + nx, y2 + ny,
            x2 - nx, y2 - ny,
            0.45f, 0.50f, 0.65f, 0.80f);
    }

    /** Emit a CCW quad. RenderType.gui() expects QUADS, so this is one primitive per call. */
    private static void quad(VertexConsumer c, Matrix4f m,
                             float x1, float y1, float x2, float y2,
                             float x3, float y3, float x4, float y4,
                             float r, float g, float b, float a) {
        c.vertex(m, x1, y1, 0).color(r, g, b, a).endVertex();
        c.vertex(m, x2, y2, 0).color(r, g, b, a).endVertex();
        c.vertex(m, x3, y3, 0).color(r, g, b, a).endVertex();
        c.vertex(m, x4, y4, 0).color(r, g, b, a).endVertex();
    }


    public static void setTickDelta(float tickDelta) {
        PoseWheelOverlay.tickDelta = tickDelta;
    }
}
