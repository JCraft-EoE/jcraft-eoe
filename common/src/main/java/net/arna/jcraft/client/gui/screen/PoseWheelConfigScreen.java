package net.arna.jcraft.client.gui.screen;

import net.arna.jcraft.client.pose.PoseDefinition;
import net.arna.jcraft.client.pose.PoseLoader;
import net.arna.jcraft.client.pose.PoseWheelConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Drag poses from the right list onto wheel slots on the left. Click a slot to clear it. */
public class PoseWheelConfigScreen extends Screen {

    // Layout
    private static final int SLOT_W = 132;
    private static final int SLOT_H = 36;
    private static final int SLOT_GAP_X = 8;
    private static final int SLOT_GAP_Y = 8;

    private static final int LIST_W = 170;
    private static final int LIST_ITEM_H = 18;
    private static final int LIST_ITEM_GAP = 2;

    private static final int BUTTON_BAR_H = 40;
    private static final int SAVE_BTN_W = 180;
    private static final int CANCEL_BTN_W = 110;
    private static final int BTN_H = 24;

    private static final int TITLE_BAR_H = 32;
    private static final int PANEL_PAD = 16;

    // Live config (mutated until Save / Cancel)
    private final String[] pendingSlots = new String[9];

    // Pose list scroll
    private int listScroll = 0;

    // Drag state
    @Nullable private String draggingPoseId = null;
    private double dragMouseX, dragMouseY;

    // Cached layout
    private int gridX, gridY;          // top-left of the 3x3 grid
    private int listX, listY, listH;   // pose list region
    private int saveBtnX, cancelBtnX, btnY;

    public PoseWheelConfigScreen() {
        super(Component.literal("Configure Pose Wheel"));
        for (int i = 0; i < 9; i++) {
            pendingSlots[i] = PoseWheelConfig.getSlot(i);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        super.init();
        int gridW = 3 * SLOT_W + 2 * SLOT_GAP_X;
        int gridH = 3 * SLOT_H + 2 * SLOT_GAP_Y;

        int contentTop = TITLE_BAR_H + PANEL_PAD;
        int contentH = height - BUTTON_BAR_H - PANEL_PAD - contentTop;

        int totalW = gridW + PANEL_PAD + LIST_W;
        int contentX = (width - totalW) / 2;

        gridX = contentX;
        gridY = contentTop + Math.max(0, (contentH - gridH) / 2);
        listX = contentX + gridW + PANEL_PAD;
        listY = contentTop;
        listH = contentH;

        btnY = height - BUTTON_BAR_H + (BUTTON_BAR_H - BTN_H) / 2;
        int btnGap = 12;
        saveBtnX = (width - (SAVE_BTN_W + btnGap + CANCEL_BTN_W)) / 2;
        cancelBtnX = saveBtnX + SAVE_BTN_W + btnGap;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // Dim the background
        gui.fill(0, 0, width, height, 0xC0101018);

        // Title bar
        gui.fill(0, 0, width, TITLE_BAR_H, 0xFF1A2233);
        gui.fill(0, TITLE_BAR_H - 1, width, TITLE_BAR_H, 0xFF4466BB);
        Font font = this.font;
        gui.drawCenteredString(font, "Configure Pose Wheel", width / 2, (TITLE_BAR_H - 8) / 2, 0xFFFFFFFF);

        // Section headers
        gui.drawString(font, "Wheel Slots", gridX, gridY - 12, 0xFFCCDDFF);
        gui.drawString(font, "Available Poses", listX, listY - 12, 0xFFCCDDFF);

        // 3x3 slot grid
        for (int i = 0; i < 9; i++) {
            int sx = gridX + (i % 3) * (SLOT_W + SLOT_GAP_X);
            int sy = gridY + (i / 3) * (SLOT_H + SLOT_GAP_Y);
            renderSlot(gui, font, sx, sy, i, mouseX, mouseY);
        }

        // Pose list (right side)
        gui.fill(listX - 2, listY - 2, listX + LIST_W + 2, listY + listH + 2, 0xFF333944);
        gui.fill(listX, listY, listX + LIST_W, listY + listH, 0xFF1A1F2A);

        List<PoseDefinition> poses = PoseLoader.getPoses();
        int visibleCount = listH / (LIST_ITEM_H + LIST_ITEM_GAP);
        int maxScroll = Math.max(0, poses.size() - visibleCount);
        listScroll = Math.max(0, Math.min(listScroll, maxScroll));

        for (int i = listScroll; i < Math.min(poses.size(), listScroll + visibleCount); i++) {
            PoseDefinition def = poses.get(i);
            int iy = listY + (i - listScroll) * (LIST_ITEM_H + LIST_ITEM_GAP) + 2;
            boolean inList = mouseX >= listX && mouseX < listX + LIST_W && mouseY >= iy && mouseY < iy + LIST_ITEM_H;
            boolean hovered = inList && draggingPoseId == null;
            boolean assigned = isAssigned(def.windupAnimId());

            int bg = hovered ? 0xFF334466 : (assigned ? 0xFF1F3322 : 0xFF252B36);
            int border = hovered ? 0xFF6688CC : (assigned ? 0xFF447744 : 0xFF3A4250);

            gui.fill(listX + 2, iy, listX + LIST_W - 2, iy + LIST_ITEM_H, bg);
            gui.renderOutline(listX + 2, iy, LIST_W - 4, LIST_ITEM_H, border);

            String name = def.displayName();
            gui.drawString(font, name, listX + 8, iy + (LIST_ITEM_H - 8) / 2,
                    assigned ? 0xFF99FF99 : 0xFFE0E0E0);
            if (def.hasIdle()) {
                gui.drawString(font, "loop", listX + LIST_W - 26, iy + (LIST_ITEM_H - 8) / 2, 0xFF88AAFF);
            }
        }

        // Scrollbar
        if (poses.size() > visibleCount) {
            int sbX = listX + LIST_W - 4;
            int sbTrackY = listY + 2;
            int sbTrackH = listH - 4;
            gui.fill(sbX, sbTrackY, sbX + 2, sbTrackY + sbTrackH, 0xFF2A3040);
            int thumbH = Math.max(8, sbTrackH * visibleCount / poses.size());
            int thumbY = sbTrackY + (maxScroll == 0 ? 0 : (sbTrackH - thumbH) * listScroll / maxScroll);
            gui.fill(sbX, thumbY, sbX + 2, thumbY + thumbH, 0xFF6688CC);
        }

        // Bottom button bar
        gui.fill(0, height - BUTTON_BAR_H, width, height - BUTTON_BAR_H + 1, 0xFF4466BB);
        gui.fill(0, height - BUTTON_BAR_H + 1, width, height, 0xFF1A2233);

        renderBigButton(gui, font, saveBtnX, btnY, SAVE_BTN_W, BTN_H,
                "Save Changes", mouseX, mouseY, 0xFF2E7D32, 0xFF4CAF50, 0xFF66BB6A);
        renderBigButton(gui, font, cancelBtnX, btnY, CANCEL_BTN_W, BTN_H,
                "Cancel", mouseX, mouseY, 0xFF44464F, 0xFF6B6E78, 0xFF8B8E98);

        // Dragging ghost — follows the cursor
        if (draggingPoseId != null) {
            String name = getDisplayName(draggingPoseId);
            int gx = (int) dragMouseX + 8;
            int gy = (int) dragMouseY - 6;
            int w = font.width(name) + 12;
            int h = 16;
            gui.fill(gx, gy, gx + w, gy + h, 0xEE2E7D32);
            gui.renderOutline(gx, gy, w, h, 0xFF99FF99);
            gui.drawString(font, name, gx + 6, gy + 4, 0xFFFFFFFF);
        }
    }

    private void renderSlot(GuiGraphics gui, Font font, int x, int y, int index, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + SLOT_W && mouseY >= y && mouseY < y + SLOT_H;
        boolean dropTarget = hovered && draggingPoseId != null;
        boolean assigned = pendingSlots[index] != null;

        int bg;
        int border;
        if (dropTarget) {
            bg = 0xFF2E5A2E;
            border = 0xFF66FF66;
        } else if (assigned) {
            bg = hovered ? 0xFF2A3344 : 0xFF1F2530;
            border = hovered ? 0xFF7799BB : 0xFF4A5466;
        } else {
            bg = hovered ? 0xFF221F2A : 0xFF161B24;
            border = hovered ? 0xFF6677AA : 0xFF353B48;
        }

        gui.fill(x, y, x + SLOT_W, y + SLOT_H, bg);
        gui.renderOutline(x, y, SLOT_W, SLOT_H, border);

        // Index badge top-left
        String idx = String.valueOf(index + 1);
        gui.fill(x + 4, y + 4, x + 18, y + 18, 0xCC000000);
        gui.drawCenteredString(font, idx, x + 11, y + 7, 0xFFFFDD44);

        // Pose name
        String poseName = assigned ? trimName(getDisplayName(pendingSlots[index]), 16) : "(empty)";
        int nameColor = assigned ? 0xFFFFFFFF : 0xFF666870;
        gui.drawString(font, poseName, x + 24, y + 8, nameColor);

        // Subtitle: animId or hint
        String sub = assigned ? trimName(pendingSlots[index], 18) : "drag a pose here";
        int subColor = assigned ? 0xFF99AABB : 0xFF555761;
        gui.drawString(font, sub, x + 24, y + 20, subColor);

        // Clear hint when assigned and hovered
        if (assigned && hovered && draggingPoseId == null) {
            String hint = "click to clear";
            int hw = font.width(hint);
            gui.drawString(font, hint, x + SLOT_W - hw - 4, y + SLOT_H - 10, 0xFFFF8888);
        }
    }

    private static void renderBigButton(GuiGraphics gui, Font font, int x, int y, int w, int h,
                                        String label, int mouseX, int mouseY,
                                        int colDim, int colNormal, int colHover) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int top = hovered ? colHover : colNormal;
        // Vertical gradient: top → bottom (darker)
        gui.fill(x, y, x + w, y + h / 2, top);
        gui.fill(x, y + h / 2, x + w, y + h, colNormal);
        // Bottom shadow strip
        gui.fill(x, y + h - 2, x + w, y + h, colDim);
        // Border
        gui.renderOutline(x, y, w, h, hovered ? 0xFFFFFFFF : 0xFFCCDDFF);
        // Label
        gui.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Save button
        if (mouseX >= saveBtnX && mouseX < saveBtnX + SAVE_BTN_W
                && mouseY >= btnY && mouseY < btnY + BTN_H) {
            save();
            onClose();
            return true;
        }
        // Cancel button
        if (mouseX >= cancelBtnX && mouseX < cancelBtnX + CANCEL_BTN_W
                && mouseY >= btnY && mouseY < btnY + BTN_H) {
            onClose();
            return true;
        }

        // Slot click (clear if assigned)
        int slotHit = getSlotAt(mouseX, mouseY);
        if (slotHit >= 0) {
            if (pendingSlots[slotHit] != null) {
                pendingSlots[slotHit] = null;
            }
            return true;
        }

        // Pose list click → begin drag
        int poseHit = getPoseAt(mouseX, mouseY);
        if (poseHit >= 0) {
            draggingPoseId = PoseLoader.getPoses().get(poseHit).windupAnimId();
            dragMouseX = mouseX;
            dragMouseY = mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingPoseId != null) {
            dragMouseX = mouseX;
            dragMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingPoseId != null && button == 0) {
            int slotHit = getSlotAt(mouseX, mouseY);
            if (slotHit >= 0) {
                pendingSlots[slotHit] = draggingPoseId;
            }
            draggingPoseId = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Only scroll when over the list
        if (mouseX >= listX && mouseX < listX + LIST_W && mouseY >= listY && mouseY < listY + listH) {
            listScroll -= (int) Math.signum(delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Helpers
    private int getSlotAt(double mx, double my) {
        for (int i = 0; i < 9; i++) {
            int sx = gridX + (i % 3) * (SLOT_W + SLOT_GAP_X);
            int sy = gridY + (i / 3) * (SLOT_H + SLOT_GAP_Y);
            if (mx >= sx && mx < sx + SLOT_W && my >= sy && my < sy + SLOT_H) return i;
        }
        return -1;
    }

    private int getPoseAt(double mx, double my) {
        if (mx < listX || mx >= listX + LIST_W) return -1;
        List<PoseDefinition> poses = PoseLoader.getPoses();
        int visibleCount = listH / (LIST_ITEM_H + LIST_ITEM_GAP);
        for (int i = listScroll; i < Math.min(poses.size(), listScroll + visibleCount); i++) {
            int iy = listY + (i - listScroll) * (LIST_ITEM_H + LIST_ITEM_GAP) + 2;
            if (my >= iy && my < iy + LIST_ITEM_H) return i;
        }
        return -1;
    }

    private void save() {
        for (int i = 0; i < 9; i++) {
            PoseWheelConfig.setSlot(i, pendingSlots[i]);
        }
        PoseWheelConfig.save();
    }

    private boolean isAssigned(String windupAnimId) {
        for (String slot : pendingSlots) {
            if (windupAnimId.equals(slot)) return true;
        }
        return false;
    }

    private String getDisplayName(String windupAnimId) {
        for (PoseDefinition def : PoseLoader.getPoses()) {
            if (def.windupAnimId().equals(windupAnimId)) return def.displayName();
        }
        return windupAnimId;
    }

    private static String trimName(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
