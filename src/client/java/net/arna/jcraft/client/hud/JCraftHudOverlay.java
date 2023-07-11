package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StandEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class JCraftHudOverlay {
    private static final Identifier EMPTY_GAUGE = new Identifier(
            JCraft.MOD_ID, "textures/gui/emptystandbar.png");
    private static final Identifier FULL_GAUGE = new Identifier(
            JCraft.MOD_ID, "textures/gui/standbar.png");
    private static final int gaugeWidth = 42;

    public static void render(MatrixStack matrixStack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int x = width / 2;

        ClientPlayerEntity player = client.player;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (!(player.getFirstPassenger() instanceof StandEntity stand)) return;
        RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
        DrawableHelper.drawTexture(matrixStack, x - gaugeWidth / 2, height - 65, 0, 0, gaugeWidth, 5, gaugeWidth, 5);
        RenderSystem.setShaderTexture(0, FULL_GAUGE);
        DrawableHelper.drawTexture(matrixStack, x - gaugeWidth / 2, height - 65, 0, 0, (int) stand.getStandGauge() * gaugeWidth / 90, 5, gaugeWidth, 5);
    }
}
