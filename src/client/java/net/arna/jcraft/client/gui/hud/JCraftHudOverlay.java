package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.MadeInHeavenEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class JCraftHudOverlay {
    private static final Identifier EMPTY_GAUGE = JCraft.id("textures/gui/empty_gauge.png");
    private static final Identifier FULL_GAUGE = JCraft.id("textures/gui/full_gauge.png");
    private static final int gaugeWidth = 42;

    public static void render(MatrixStack matrixStack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int x = width / 2;

        ClientPlayerEntity player = client.player;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        //TODO: once we require more than two gauges, redo this in a more modular and scalable way ( record Gauge(){}, refreshGauges(), updateGauges() )
        if (player.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
            int gaugeX = x - gaugeWidth / 2;

            RenderSystem.setShaderColor(0.5f, 0.5f, 1, 1);
            RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
            DrawableHelper.drawTexture(matrixStack, gaugeX, height - 65, 0, 0, gaugeWidth, 5, gaugeWidth, 5);
            RenderSystem.setShaderTexture(0, FULL_GAUGE);
            DrawableHelper.drawTexture(matrixStack, gaugeX, height - 65, 0, 0, (int) stand.getStandGauge() * gaugeWidth / 90, 5, gaugeWidth, 5);

            if (stand instanceof MadeInHeavenEntity madeInHeaven && madeInHeaven.getAccelTime() > 0) {
                RenderSystem.setShaderColor(1, 0.8f, 0, 1);
                RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
                DrawableHelper.drawTexture(matrixStack, gaugeX, height - 71, 0, 0,
                        gaugeWidth, 5, gaugeWidth, 5);
                RenderSystem.setShaderTexture(0, FULL_GAUGE);
                DrawableHelper.drawTexture(matrixStack, gaugeX, height - 71, 0, 0,
                        madeInHeaven.getSpeedometer() * gaugeWidth / MadeInHeavenEntity.MAXIMUM_SPEEDOMETER, 5, gaugeWidth, 5);
            }
        }
    }


}
