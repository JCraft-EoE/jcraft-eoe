package net.arna.jcraft.client.gui.screen;

import net.arna.jcraft.client.gui.hud.PoseWheelOverlay;
import net.arna.jcraft.client.pose.PoseWheelConfig;
import net.arna.jcraft.client.pose.PoseWheelState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class PoseWheelScreen extends Screen {

    public PoseWheelScreen() {
        super(Component.literal("Pose Wheel"));
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        PoseWheelState.markOpen(true);
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        PoseWheelOverlay.render(gui, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics gui) {
        // keep the world visible behind the wheel
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (PoseWheelOverlay.tryClickConfigButton(Objects.requireNonNull(this.minecraft))) return true;
            int hovered = PoseWheelState.getHoveredSlot();
            if (hovered >= 0) {
                triggerSlotAndClose(hovered);
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Objects.requireNonNull(this.minecraft).setScreen(new PoseWheelConfigScreen());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            triggerSlotAndClose(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Objects.requireNonNull(this.minecraft).setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void triggerSlotAndClose(int slotIndex) {
        Player player = Objects.requireNonNull(this.minecraft).player;
        if (PoseWheelConfig.getPoseAt(slotIndex) == null) {
            if (player != null) {
                player.displayClientMessage(
                    Component.literal("Move not configured for slot " + (slotIndex + 1)), true);
            }
        } else {
            PoseWheelState.executeSlot(slotIndex);
        }
        this.minecraft.setScreen(null);
    }

    @Override
    public void removed() {
        PoseWheelState.markOpen(false);
        super.removed();
    }
}
