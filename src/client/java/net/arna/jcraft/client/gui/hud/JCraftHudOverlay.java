package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class JCraftHudOverlay {
    private static final Identifier EMPTY_GAUGE = JCraft.id("textures/gui/empty_gauge.png");
    private static final Identifier FULL_GAUGE = JCraft.id("textures/gui/full_gauge.png");
    private static final int gaugeWidth = 42;
    private static int gaugeHeightOffset;
    private static final int gaugeHeightOffsetMax = -65;
    private static final Gauge BLOCK_GAUGE = new Gauge(0.5f, 0.5f, 1.0f, 90);
    private static final Gauge SUN_SIZE_GAUGE = new Gauge(1.0f, 0.7f, 0.4f, 30);
    private static final Gauge TIME_ACCEL_GAUGE = new Gauge(1.0f, 0.8f, 0.0f, MadeInHeavenEntity.MAXIMUM_SPEEDOMETER);
    private static final Gauge BLOODLUST_GAUGE = new Gauge(0.8f, 0.1f, 0.2f, 5);

    public static void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int x = width / 2;

        ClientPlayerEntity player = client.player;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        gaugeHeightOffset = gaugeHeightOffsetMax;
        int gaugeX = x - gaugeWidth / 2;

        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null) {
            if (stand instanceof TheSunEntity theSun) {
                float darken = (theSun.isPassive() ? 0.4f : 0.0f);
                SUN_SIZE_GAUGE.render(ctx,
                        SUN_SIZE_GAUGE.red() - darken,
                        SUN_SIZE_GAUGE.green() - darken,
                        SUN_SIZE_GAUGE.blue() - darken,
                        gaugeX,
                        height + gaugeHeightOffset,
                        (int) (theSun.getScale() * 10.0F));
            } else
                BLOCK_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, (int) stand.getStandGauge());
            if (stand instanceof MadeInHeavenEntity madeInHeaven && madeInHeaven.getAccelTime() > 0)
                TIME_ACCEL_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, madeInHeaven.getSpeedometer());
        }

        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec instanceof AnubisSpec) {
            int displayBloodlust = (int) ((JComponents.getMiscData(player).getAttackSpeedMult() - 1.0f) * 5);
            if (displayBloodlust > 0)
                BLOODLUST_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, displayBloodlust);
        }
    }

    protected record Gauge(float red, float green, float blue, @Setter int max) {
        public Gauge(Vector3f color, int max) {
            this(color.x(), color.y(), color.z(), max);
        }

        public void render(DrawContext ctx, int x, int y, int value) {
            render(ctx, red, green, blue, x, y, value);
        }

        public void render(DrawContext ctx, float r, float g, float b, int x, int y, int value) {
            RenderSystem.setShaderColor(r, g, b, 1);
            //RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
            ctx.drawTexture(EMPTY_GAUGE, x, y, 0, 0, gaugeWidth, 5, gaugeWidth, 5);
            //RenderSystem.setShaderTexture(0, FULL_GAUGE);
            ctx.drawTexture(FULL_GAUGE, x, y, 0, 0, value * gaugeWidth / max, 5, gaugeWidth, 5);
            gaugeHeightOffset -= 6;
        }
    }
}
