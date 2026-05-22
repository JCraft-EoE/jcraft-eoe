package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
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
 * Visuals for the pose wheel. Segments, hover/active highlighting, labels, animated
 * player preview, and the "Configure Poses" button. Owns no input or lifecycle; see
 * {@link net.arna.jcraft.client.gui.screen.PoseWheelScreen} for that, and
 * {@link PoseWheelState} for the open/hover/active state both sides read
 */
public class PoseWheelOverlay extends AbstractWheelHUD {

    private static final int SLOTS = 9;
    private static final int OUTER_RADIUS = 120;
    private static final int INNER_RADIUS = 44;
    private static final int CENTER_CIRCLE_RADIUS = 38;
    private static final int PLAYER_PREVIEW_SCALE = 30;

    private static final int BTN_W = 80;
    private static final int BTN_H = 16;
    private static final int BTN_MARGIN = 6;

    public static final PoseWheelOverlay INSTANCE = new PoseWheelOverlay();

    @Getter
    private static float tickDelta;

    @Nullable private static KeyframeAnimationPlayer previewAnimPlayer = null;
    private static int lastPreviewSlot = -2;
    private static long previewLastTickMs = 0L;

    private PoseWheelOverlay() {
        super(SLOTS, OUTER_RADIUS, INNER_RADIUS, CENTER_CIRCLE_RADIUS);
    }

    @Override
    public void render(GuiGraphics gui, float partialTick) {
        if (!PoseWheelState.isOpen()) return;

        // Sync hover state with PoseWheelState (which click handlers read).
        this.hoveredSlot = PoseWheelState.getHoveredSlot();
        super.render(gui, partialTick);
        PoseWheelState.setHoveredSlot(this.hoveredSlot);

        // Configure button — wheel-specific, sits in the bottom-right of the scaled overlay.
        Minecraft mc = Minecraft.getInstance();
        double ratio = FIXED_GUI_SCALE / mc.getWindow().getGuiScale();
        gui.pose().pushPose();
        gui.pose().scale((float) ratio, (float) ratio, 1f);
        int scaledW = (int) (mc.getWindow().getWidth() / FIXED_GUI_SCALE);
        int scaledH = (int) (mc.getWindow().getHeight() / FIXED_GUI_SCALE);
        renderConfigButton(gui, mc.font,
                scaledW - BTN_W - BTN_MARGIN,
                scaledH - BTN_H - BTN_MARGIN,
                mc, ratio);
        gui.pose().popPose();
    }

    // ---- Wheel-specific behavior ----

    @Override
    protected boolean isActiveSlot(int index) {
        PoseDefinition def = PoseWheelConfig.getPoseAt(index);
        if (def == null) return false;
        String active = PoseWheelState.getActivePoseAnimId();
        if (active == null) return false;
        return active.equals(def.windupAnimId())
                || (def.idleAnimId() != null && active.equals(def.idleAnimId()));
    }

    @Override
    protected String labelBottom(int index) {
        PoseDefinition def = PoseWheelConfig.getPoseAt(index);
        return def != null ? trimName(def.displayName()) : "—";
    }

    @Override
    protected int labelBottomColor(int index, boolean hovered) {
        PoseDefinition def = PoseWheelConfig.getPoseAt(index);
        return hovered ? 0xFFFFDD44 : (def != null ? 0xFFFFFFFF : 0xFF888888);
    }

    @Override
    protected void renderCenter(GuiGraphics gui, int cx, int cy, float tickDelta) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        renderPlayerPreview(gui, cx, cy, player, tickDelta);
    }

    // ---- Static helpers (kept for PoseWheelScreen to call into) ----

    /** Returns true if the click landed on the configure button (and opens the config screen). */
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

    public static void setTickDelta(float td) { PoseWheelOverlay.tickDelta = td; }

    // ---- Player preview ----

    private static void renderPlayerPreview(GuiGraphics gui, int cx, int cy, Player player, float td) {
        PoseWheelOverlay.tickDelta = td;
        Quaternionf poseRot = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(0f);

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

        float sBody = player.yBodyRot, sBodyO = player.yBodyRotO;
        float sYaw = player.getYRot(), sYawO = player.yRotO;
        float sPitch = player.getXRot(), sPitchO = player.xRotO;
        float sHead = player.yHeadRot, sHeadO = player.yHeadRotO;

        player.yBodyRot = 180f; player.yBodyRotO = 180f;
        player.setYRot(180f);   player.yRotO = 180f;
        player.setXRot(0f);     player.xRotO = 0f;
        player.yHeadRot = 180f; player.yHeadRotO = 180f;

        // Only swap in a preview animation if the player has nothing playing — never
        // overwrite an active pose, that interrupts the looping JCraft animation system.
        boolean swappedPreview = false;
        if (previewAnimPlayer != null && layer.getAnimation() == null) {
            layer.setAnimation(previewAnimPlayer);
            swappedPreview = true;
        }
        renderEntityInCenter(gui, cx, cy + CENTER_CIRCLE_RADIUS - 6, PLAYER_PREVIEW_SCALE, poseRot, camera, player);
        if (swappedPreview) layer.setAnimation(null);

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
}
