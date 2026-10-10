package net.arna.jcraft.client.gui.screen;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.menu.DiscCaseMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class DiscCaseMenuScreen extends AbstractContainerScreen<DiscCaseMenu> {

    private static final ResourceLocation DISC_CASE_BACKGROUND = JCraft.id("textures/gui/disc_case_menu_screen.png");

    private final int slots;

    public DiscCaseMenuScreen(final DiscCaseMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        slots = menu.getSlotCount();
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        guiGraphics.blit(DISC_CASE_BACKGROUND, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(DISC_CASE_BACKGROUND, leftPos + 7 + 9 * (9 - slots / 2), topPos + 26, 7, 83, 9 * slots, 36);
    }

}
