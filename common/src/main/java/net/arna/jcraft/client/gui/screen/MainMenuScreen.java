package net.arna.jcraft.client.gui.screen;

import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class MainMenuScreen extends Screen {

    protected StandEntity<?,?> stand;

    public MainMenuScreen() {
        super(Component.literal("hey"));
        stand = JUtils.getStand(Minecraft.getInstance().player);
    }

    // add components in this method
    @Override
    protected void init() {
    }

    // draw text in this method
    @Override
    public void render(final @NonNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // TODO make the strings translatables
        guiGraphics.drawString(font, "normals", 10, height/7 + 10, 0xFFFFFF);
        guiGraphics.drawString(font, "specials", width/4 + 10, height/7 + 10, 0xFFFFFF);
        final MoveMap<?,?> moveMap = MoveSetManager.get(JStandTypeRegistry.STAR_PLATINUM.get()).get("default").getMoveMap();
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.LIGHT)) {
            drawMoveString(guiGraphics, font, entry, false, 10, 2*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.HEAVY)) {
            drawMoveString(guiGraphics, font, entry, false, 10, 3*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.BARRAGE)) {
            drawMoveString(guiGraphics, font, entry, false, 10, 4*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.UTILITY)) {
            drawMoveString(guiGraphics, font, entry, false, 10, 5*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.SPECIAL1)) {
            drawMoveString(guiGraphics, font, entry, false, width/4 + 10, 2*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.SPECIAL2)) {
            drawMoveString(guiGraphics, font, entry, false, width/4 + 10, 3*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.SPECIAL3)) {
            drawMoveString(guiGraphics, font, entry, false, width/4 + 10, 4*height/7 + 10, 0xFFFFFF);
        }
        for (MoveMap.Entry<?,?> entry : moveMap.getEntries(MoveClass.ULTIMATE)) {
            drawMoveString(guiGraphics, font, entry, false, width/4 + 10, 5*height/7 + 10, 0xFFFFFF);
        }
    }

    protected static void drawMoveString(final @NonNull GuiGraphics guiGraphics, final @NonNull Font font, final @NonNull MoveMap.Entry<?,?> move, final boolean isVariant, final int x, final int y, final int color) {
        guiGraphics.drawString(font, move.getMove().getName().copy().withStyle(ChatFormatting.DARK_PURPLE), x, y, color);
        final Component text = Component.empty()
                .append(move.getMoveClass().getFriendlyName())
                .append(Component.empty()
                        .append(Component.literal(" ("))
                        .append(move.getMoveClass().getKey().copy().withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(")")));
        guiGraphics.drawString(font, text, x, y + 10, color);
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
