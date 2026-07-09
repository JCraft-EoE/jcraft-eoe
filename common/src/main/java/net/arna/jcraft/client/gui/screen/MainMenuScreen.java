package net.arna.jcraft.client.gui.screen;

import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class MainMenuScreen extends Screen {

    protected StandEntity<?,?> stand;

    public MainMenuScreen() {
        super(Component.literal("hey"));
        stand = JUtils.getStand(Minecraft.getInstance().player);
    }

    @Override
    protected void init() {
    }

    //    @Override
//    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
//        guiGraphics.blit(JCraft.id("textures/gui/menu_screen.png"), leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
//    }
//
//    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
//        int row = 0;
//        drawText(guiGraphics, this.title, row++, 0);
//        if (stand == null || StandTypeUtil.isNone(stand.getStandType())) {
//            drawText(guiGraphics, JStandTypeRegistry.NONE.get().getData().getInfo().getName(), row++, 0);
//        }
//        else {
//            StandInfo info = stand.getStandData().getInfo();
//            drawText(guiGraphics, info.getName(), row++, 0);
//            final String desc = String.format("entity.%s.%s%s.info.desc", JCraft.MOD_ID, info.getName(),
//                    stand.getModeOrdinal() == 0 ? "" : Integer.toString(stand.getModeOrdinal()));
//            drawText(guiGraphics, Component.translatable(desc), row++, 0);
//            drawText(guiGraphics, Component.literal("PROS"), row++, 0);
//            final int pros = info.getProCount();
//            final String proFormatted = "entity.%s.%s.info.pro%d";
//            for (int p = 1; p <= pros; p++) {
//                final Component pro = Component.literal("● ").append(Component.translatable(
//                        String.format(proFormatted, JCraft.MOD_ID, info.getNameKey(), p)));
//                drawText(guiGraphics, pro, row++, 0);
//            }
//            drawText(guiGraphics, Component.literal("CONS"), row++, 0);
//            final int cons = info.getConCount();
//            final String conFormatted = "entity.%s.%s.info.con%d";
//            for (int c = 1; c <= cons; c++) {
//                final Component pro = Component.literal("● ").append(Component.translatable(
//                        String.format(conFormatted, JCraft.MOD_ID, info.getNameKey(), c)));
//                drawText(guiGraphics, pro, row++, 0);
//            }
//        }
//    }
//
//    protected void drawText(final GuiGraphics guiGraphics, final Component text, final int row, final int col) {
//        drawText(guiGraphics, text, row, col, 4210752);
//    }
//
//    protected void drawText(final GuiGraphics guiGraphics, final Component text, final int row, final int col, final int color) {
//        guiGraphics.drawString(this.font, text, this.titleLabelX+10*col, this.titleLabelY+10*row, color, false);
//    }
}
