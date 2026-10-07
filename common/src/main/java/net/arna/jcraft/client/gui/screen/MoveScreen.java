package net.arna.jcraft.client.gui.screen;

import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MoveScreen extends Screen {

    public static final Component MOVE_VARIANTS = Component.translatable("jcraft.gui.move_variants");

    @NonNull
    protected final IAttacker<?,?> attacker;
    protected final MoveMap.Entry<?,?> baseMove;

    /**
     * Making sure that the given stand has the given attack is the responsibility of the caller.
     */
    public MoveScreen(final @NonNull IAttacker<?,?> attacker, final MoveMap.Entry<?, ?> baseMove) {
        super(Component.literal("JCraft Move Screen"));
        this.attacker = attacker;
        this.baseMove = baseMove;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(new MainMenuScreen());
    }

    @Override
    public void tick() {
        super.tick();
        if (attacker instanceof final StandEntity<?,?> stand) {
            stand.tick();
        }
    }

    // draw text in this method
    @Override
    public void render(final @NonNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // takes care of widgets and such
        guiGraphics.drawString(font, MOVE_VARIANTS, 10, height/7 + 10, 0xFFFFFF);
        drawMoveString(guiGraphics, font, baseMove, 10, 2*height/7 + 10, 0xFFFFFF);
    }

    protected void drawMoveString(final @NonNull GuiGraphics guiGraphics, final @NonNull Font font, final @NonNull MoveMap.Entry<?,?> move, final int x, final int y, final int color) {
        int currentY = y;
        guiGraphics.drawString(font, move.getMove().getName().copy().withStyle(ChatFormatting.DARK_PURPLE), x, currentY, color);
        Component text = Component.empty()
                .append(move.getMoveClass().getFriendlyName())
                .append(Component.empty()
                        .append(Component.literal(" ("))
                        .append(move.getMoveClass().getKey().copy().withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(")")));
        guiGraphics.drawString(font, text, x, currentY + 10, color);
        if (move.getCrouchingVariant() != null) {
            currentY += height/7;
            guiGraphics.drawString(font, move.getCrouchingVariant().getMove().getName().copy().withStyle(ChatFormatting.DARK_PURPLE), x, currentY, color);
            text = Component.empty()
                    .append(move.getMoveClass().getFriendlyName())
                    .append(Component.empty()
                            .append(Component.literal(" ("))
                            .append(Component.translatable("jcraft.gui.crouching"))
                            .append(Component.literal(" + "))
                            .append(move.getMoveClass().getKey().copy().withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(")")));
            guiGraphics.drawString(font, text, x, currentY + 10, color);
        }
        if (move.getAerialVariant() != null) {
            currentY += height/7;
            guiGraphics.drawString(font, move.getAerialVariant().getMove().getName().copy().withStyle(ChatFormatting.DARK_PURPLE), x, currentY, color);
            text = Component.empty()
                    .append(move.getMoveClass().getFriendlyName())
                    .append(Component.empty()
                            .append(Component.literal(" ("))
                            .append(Component.translatable("jcraft.gui.aerial"))
                            .append(Component.literal(" + "))
                            .append(move.getMoveClass().getKey().copy().withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(")")));
            guiGraphics.drawString(font, text, x, currentY + 10, color);
        }
    }

}
