package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class JCraftHudOverlay {
    private static final Identifier EMPTY_GAUGE = JCraft.id("textures/gui/empty_gauge.png");
    private static final Identifier FULL_GAUGE = JCraft.id("textures/gui/full_gauge.png");
    private static final int gaugeWidth = 42;
    private static int gaugeHeightOffset;
    private static final int gaugeHeightOffsetMax = -65;
    private static final Gauge BLOCK_GAUGE = new Gauge(0.5f, 0.5f, 1.0f, 90);
    private static final Gauge TIME_ACCEL_GAUGE = new Gauge(1.0f, 0.8f, 0.0f, MadeInHeavenEntity.MAXIMUM_SPEEDOMETER);
    private static final Gauge BLOODLUST_GAUGE = new Gauge(0.8f, 0.1f, 0.2f, 5);

    public static void render(MatrixStack matrixStack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int x = width / 2;

        ClientPlayerEntity player = client.player;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        gaugeHeightOffset = gaugeHeightOffsetMax;
        int gaugeX = x - gaugeWidth / 2;

        if (player.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
            BLOCK_GAUGE.render(matrixStack, gaugeX, height + gaugeHeightOffset, (int) stand.getStandGauge());
            if (stand instanceof MadeInHeavenEntity madeInHeaven && madeInHeaven.getAccelTime() > 0)
                TIME_ACCEL_GAUGE.render(matrixStack, gaugeX, height + gaugeHeightOffset, madeInHeaven.getSpeedometer());
        }

        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec instanceof AnubisSpec) {
            int displayBloodlust = (int) ((JComponents.getMiscData(player).getAttackSpeedMult() - 1.0f) * 5);
            if (displayBloodlust > 0)
                BLOODLUST_GAUGE.render(matrixStack, gaugeX, height + gaugeHeightOffset, displayBloodlust);
        }
    }

    protected record Gauge(float red, float green, float blue, @Setter int max) {
        public Gauge(Vec3f color, int max) {
            this(color.getX(), color.getY(), color.getZ(), max);
        }

        public void render(MatrixStack matrixStack, int x, int y, int value) {
            RenderSystem.setShaderColor(red, green, blue, 1);
            RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
            DrawableHelper.drawTexture(matrixStack, x, y, 0, 0, gaugeWidth, 5, gaugeWidth, 5);
            RenderSystem.setShaderTexture(0, FULL_GAUGE);
            DrawableHelper.drawTexture(matrixStack, x, y, 0, 0, value * gaugeWidth / max, 5, gaugeWidth, 5);
            gaugeHeightOffset -= 6;
        }
    }
}
