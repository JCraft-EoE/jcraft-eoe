package net.arna.jcraft.client.gui.screen;

import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MainMenuScreen extends Screen {

    protected StandEntity<?,?> stand;
    protected List<Integer> skins;

    protected Button equipSkinBtn;

    public MainMenuScreen() {
        super(Component.literal("hey")); // TODO change this
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            skins = List.of();
            return;
        }
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(player);
        final StandType type = standData.getType();
        if (!StandTypeUtil.isNone(type)) {
            stand = type.createEntity(player.level());
            final int skin = standData.getSkin();
            stand.setSkin(skin);
            stand.setVirtual(true);
            stand.tickCount = stand.getStandData().getSummonData().getAnimDuration() + 1;
            stand.setRawState(stand.getIdleState().ordinal());
        }
        skins = standData.getSkinsFor(type);
    }

    // add components in this method
    @Override
    protected void init() {
        final BnbList bnbList = new BnbList(Minecraft.getInstance(), width / 4, height / 2, height / 2, height, 9*2);
        bnbList.setLeftPos(width/2);
        addRenderableWidget(bnbList);
        final Button prevSkinBtn = Button.builder(Component.literal("<"), button -> setDisplayedSkin(stand.getSkin()-1))
                .bounds(3*width/4, 6*height/7 + 10, Math.max(2, width/12), Math.max(2, height/7 - 10))
                .build();
        addRenderableWidget(prevSkinBtn);
        // TODO make this string translatable
        equipSkinBtn = Button.builder(Component.literal("apply"), button -> applySkin())
                .bounds(10*width/12, 6*height/7 + 10, Math.max(2, width/12), Math.max(2, height/7 - 10))
                .build();
        addRenderableWidget(equipSkinBtn);
        final Button nextSkinBtn = Button.builder(Component.literal(">"), button -> setDisplayedSkin(stand.getSkin()+1))
                .bounds(11*width/12, 6*height/7 + 10, Math.max(2, width/12), Math.max(2, height/7 - 10))
                .build();
        addRenderableWidget(nextSkinBtn);
    }

    void setDisplayedSkin(int skin) {
        final int skinCount = stand.getStandData().getInfo().getSkinCount();
        final int actualSkin = (skin + skinCount) % skinCount;
        stand.setSkin(actualSkin);
        if (equipSkinBtn != null) {
            equipSkinBtn.active = skins.contains(actualSkin);
        }
    }

    void applySkin() {
        final int skin = stand.getSkin();
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(player);
        final StandType type = standData.getType();
        if (!StandTypeUtil.isNone(type)) {
            standData.setSkin(skin);
            StandEntity<?,?> standEntity = JUtils.getStand(player);
            if (standEntity != null) {
                standEntity.setSkin(skin);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (stand != null) {
            stand.tick();
        }
    }

    // draw text in this method
    @Override
    public void render(final @NonNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // takes care of the scroll widget and such
        if (stand != null) {
            // TODO make the strings translatables
            guiGraphics.drawString(font, "normals", 10, height/7 + 10, 0xFFFFFF);
            guiGraphics.drawString(font, "specials", width/4 + 10, height/7 + 10, 0xFFFFFF);
            final MoveMap<?,?> moveMap = stand.getMoveMap();
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
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, 7*width/8, 8*height/14 + 10, 45, 7f*width/8 - mouseX, 8f*height/14 + 10 - mouseY, stand);
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

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (stand != null && mouseX >= 3f*width/4 && mouseY >= height/7f + 10 && mouseY < 6f*height/7 + 10) {
            final SoundEvent summonSound = stand.getStandData().getSummonData().getSound();
            final LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && summonSound != null) {
                player.playSound(summonSound);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    @Environment(EnvType.CLIENT)
    class BnbList extends ObjectSelectionList<BnbList.BnbListEntry> {

        public BnbList(final Minecraft minecraft, final int width, final int height, final int y0, final int y1, final int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
            addEntry(new BnbListEntry());
        }

        @Override
        public int getRowWidth() {
            return MainMenuScreen.this.width/4;
        }

        @Override
        protected int getScrollbarPosition() {
            return 3*MainMenuScreen.this.width/4 - 6; // -6 to ensure it completely stays in the 3rd quarter
        }

        @Environment(EnvType.CLIENT)
        class BnbListEntry extends ObjectSelectionList.Entry<BnbListEntry> {

            @NonNull
            @Override
            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(final GuiGraphics guiGraphics, final int index, final int top, final int left, final int width, final int height, final int mouseX, final int mouseY, final boolean hovering, final float partialTick) {
                guiGraphics.drawString(font, Component.literal("this is a test " + index + ", baka!"), left+2, top+1, 16777215);
            }
        }

    }
}
