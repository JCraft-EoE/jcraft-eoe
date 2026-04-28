package net.arna.jcraft.client.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class JCraftHudOverlay {
    private static final ResourceLocation EMPTY_GAUGE = JCraft.id("textures/gui/empty_gauge.png");
    private static final ResourceLocation FULL_GAUGE = JCraft.id("textures/gui/full_gauge.png");
    private static final int gaugeWidth = 42;
    private static int gaugeHeightOffset;
    private static final int GAUGE_HEIGHT_OFFSET_MAX = -65;
    private static int lastStandGauge = 90;
    private static int standGaugeFlashTicks = 0;
    private static final int STAND_GAUGE_FLASH_DURATION = 60;

    private static final Vector3f
            BLOCK_GAUGE_UNDER_THIRD = new Vector3f(0.5f, 0.5f, 1.0f),
            BLOCK_GAUGE_OVER_THIRD = new Vector3f(0.55f, 0.8f, 1.0f);

    private static final Gauge
            BLOCK_GAUGE = new Gauge(0.5f, 0.5f, 1.0f, 90),
            SUN_SIZE_GAUGE = new Gauge(1.0f, 0.7f, 0.4f, 30),
            TIME_ACCEL_GAUGE = new Gauge(1.0f, 0.8f, 0.0f, MadeInHeavenEntity.MAXIMUM_SPEEDOMETER),
            BLOODLUST_GAUGE = new Gauge(0.8f, 0.1f, 0.2f, 5),
            HAMON_GAUGE = new Gauge(0.8f, 0.5f, 0.2f, (int) HamonSpec.MAX_CHARGE),
            IRON_GAUGE = new Gauge(0.7f, 0.7f, 0.9f, (int) MetallicaEntity.IRON_MAX),
            OVERHEAT_GAUGE = new Gauge(0.8f, 0.1f, 0.2f, (int) AerosmithEntity.OVERHEAT_MAX);

    public static void render(final GuiGraphics ctx) {
        final Minecraft client = Minecraft.getInstance();

        final int width = client.getWindow().getGuiScaledWidth();
        final int height = client.getWindow().getGuiScaledHeight();
        final int x = width / 2;

        final LocalPlayer player = client.player;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        gaugeHeightOffset = GAUGE_HEIGHT_OFFSET_MAX;
        final int gaugeX = x - gaugeWidth / 2;

        if (client.gui.overlayMessageTime > 0) gaugeHeightOffset -= 12;

        final StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null) {
            standGaugeFlashTicks--;

            if (stand instanceof TheSunEntity theSun) {
                final float darken = (theSun.isPassive() ? 0.4f : 0.0f);
                SUN_SIZE_GAUGE.render(ctx,
                        SUN_SIZE_GAUGE.red() - darken,
                        SUN_SIZE_GAUGE.green() - darken,
                        SUN_SIZE_GAUGE.blue() - darken,
                        gaugeX,
                        height + gaugeHeightOffset,
                        (int) (theSun.getRawScale() * 10.0F));
            } else {
                final float standGauge = stand.getStandGauge();
                Vector3f color = standGauge > stand.getMaxStandGauge() / 3.0f ?
                        BLOCK_GAUGE_OVER_THIRD :
                        BLOCK_GAUGE_UNDER_THIRD;

                color = new Vector3f(color); // Writable copy

                final int intStandGauge = (int) standGauge;

                if (intStandGauge > lastStandGauge) {
                    standGaugeFlashTicks = STAND_GAUGE_FLASH_DURATION;
                }

                if (standGaugeFlashTicks > 0) {
                    float lighten = standGaugeFlashTicks / (float)STAND_GAUGE_FLASH_DURATION;
                    color.add(lighten, lighten, lighten);
                }

                BLOCK_GAUGE.render(ctx, color.x, color.y, color.z, gaugeX, height + gaugeHeightOffset, intStandGauge);

                lastStandGauge = intStandGauge;
            }
            if (stand instanceof MadeInHeavenEntity madeInHeaven && madeInHeaven.getAccelTime() > 0) {
                TIME_ACCEL_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, madeInHeaven.getSpeedometer());
            }
            if (stand instanceof MetallicaEntity metallica) {
                IRON_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, (int) metallica.getIron());
            }
            if (stand instanceof AerosmithEntity aerosmith) {
                OVERHEAT_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, (int) aerosmith.getOverheat());
            }
        }

        // don't display spec gauges in spectator
        if (player == null || player.isSpectator()) {
            return;
        }
        final JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec instanceof AnubisSpec) {
            final int displayBloodlust = (int) ((JComponentPlatformUtils.getMiscData(player).getAttackSpeedMult() - 1.0f) * 5);
            if (displayBloodlust > 0) {
                BLOODLUST_GAUGE.render(ctx, gaugeX, height + gaugeHeightOffset, displayBloodlust);
            }
        } else if (spec instanceof HamonSpec) {
            final var hamon = JComponentPlatformUtils.getHamon(player);
            final int charge = (int) hamon.getHamonCharge();
            final Vector3f color = hamon.isHamonizeReady() ? new Vector3f(1.0f, 1.0f, 0.6f) : HAMON_GAUGE.colorCopy();
            HAMON_GAUGE.render(ctx, color.x, color.y, color.z, gaugeX, height + gaugeHeightOffset, charge);
        }
    }

    protected record Gauge(float red, float green, float blue, int max) {
        public Gauge(Vector3f color, int max) {
            this(color.x(), color.y(), color.z(), max);
        }

        public void render(GuiGraphics ctx, int x, int y, int value) {
            render(ctx, red, green, blue, x, y, value);
        }

        public void render(GuiGraphics ctx, float r, float g, float b, int x, int y, int value) {
            RenderSystem.setShaderColor(r, g, b, 1);
            //RenderSystem.setShaderTexture(0, EMPTY_GAUGE);
            ctx.blit(EMPTY_GAUGE, x, y, 0, 0, gaugeWidth, 5, gaugeWidth, 5);
            //RenderSystem.setShaderTexture(0, FULL_GAUGE);
            ctx.blit(FULL_GAUGE, x, y, 0, 0, value * gaugeWidth / max, 5, gaugeWidth, 5);
            gaugeHeightOffset -= 6;
            RenderSystem.setShaderColor(1, 1, 1, 1f);
        }

        public Vector3f colorCopy() {
            return new Vector3f(red, green, blue);
        }
    }
}
