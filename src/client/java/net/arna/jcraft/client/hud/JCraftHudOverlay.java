package net.arna.jcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StandEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class JCraftHudOverlay implements HudRenderCallback {
    private static final Identifier EMPTY_GAUGE = new Identifier(
            JCraft.MOD_ID, "textures/gui/emptystandbar.png");
    private static final Identifier FULL_GAUGE = new Identifier(
            JCraft.MOD_ID, "textures/gui/standbar.png");
    private final int gaugeWidth = 42;

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        int x;
        int y;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            x = width / 2;
            y = height;

            ClientPlayerEntity player = client.player;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            if (player.getFirstPassenger() instanceof StandEntity stand) {
                RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
                DrawableHelper.drawTexture(matrixStack, x - gaugeWidth / 2, y - 65, 0, 0, gaugeWidth, 5, gaugeWidth, 5);
                RenderSystem.setShaderTexture(0, FULL_GAUGE);
                DrawableHelper.drawTexture(matrixStack, x - gaugeWidth / 2, y - 65, 0, 0, (int) stand.getStandGauge() * gaugeWidth / 90, 5, gaugeWidth, 5);
            }
        }
    }
}
