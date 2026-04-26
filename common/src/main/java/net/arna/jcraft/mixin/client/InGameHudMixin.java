package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.gui.hud.JCraftHudOverlay;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
    @Unique
    private static final ResourceLocation EMPTY_BLOOD_ICON = JCraft.id("textures/gui/blood_empty.png");
    @Unique
    private static final ResourceLocation HALF_BLOOD_ICON = JCraft.id("textures/gui/blood_half.png");
    @Unique
    private static final ResourceLocation FULL_BLOOD_ICON = JCraft.id("textures/gui/blood_full.png");
    @Unique
    private ResourceLocation jcraft$currentBloodIcon = EMPTY_BLOOD_ICON;

    @WrapOperation(
            method = "renderPlayerHealth",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            ),
            require = 3
    )
    void showVampireBloodIcons(GuiGraphics instance, ResourceLocation atlasLocation,
                               int x, int y, int uOffset, int vOffset, int uWidth, int vHeight, Operation<Void> original) {
        final Player player = minecraft.player;

        if (JComponentPlatformUtils.getVampirism(player).isVampire()) {
            instance.blit(jcraft$currentBloodIcon, x, y, 0, 0, uWidth, vHeight, 9, 9);
        } else {
            original.call(instance, atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
        }
    }

    @Inject(
            method = "renderPlayerHealth",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 0
            )
    )
    void switchToEmptyBloodIcon(GuiGraphics context, CallbackInfo ci) {
        jcraft$currentBloodIcon = EMPTY_BLOOD_ICON;
    }

    @Inject(
            method = "renderPlayerHealth",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 2
            )
    )
    void switchToHalfBloodIcon(GuiGraphics context, CallbackInfo ci) {
        jcraft$currentBloodIcon = HALF_BLOOD_ICON;
    }

    @Inject(
            method = "renderPlayerHealth",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 1
            )
    )
    void switchToFullBloodIcon(GuiGraphics context, CallbackInfo ci) {
        jcraft$currentBloodIcon = FULL_BLOOD_ICON;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private void renderEpitaph(GuiGraphics context, float tickDelta, CallbackInfo ci) {
        if (JClientConfig.getInstance().isEpitaphOverlay()) {
            EpitaphOverlay.render();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void cancelXpBarForWeatherMeter(GuiGraphics ctx, int x, CallbackInfo ci) {
        final LocalPlayer player = minecraft.player;
        if (player == null) return;
        if (!(JUtils.getStand(player) instanceof WeatherReportEntity weatherReport)) return;
        if (weatherReport.getWeatherMeter() <= 0) return;
        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderWeatherMeter(GuiGraphics ctx, float tickDelta, CallbackInfo ci) {
        final LocalPlayer player = minecraft.player;
        if (player == null) return;
        if (!(JUtils.getStand(player) instanceof WeatherReportEntity weatherReport)) return;

        final float meter = weatherReport.getWeatherMeter();
        if (meter <= 0) return;

        final int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        final int barX = screenWidth / 2 - 91;
        final int barY = screenHeight - 29;

        // Background (empty xp bar)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ctx.blit(ICONS, barX, barY, 0, 64, 182, 5);

        // Color: light blue (low charge) → deep blue (full charge)
        final float hue = 0.6f;
        final float sat = 0.35f + meter * 0.65f;
        final float val = 1.0f - meter * 0.35f;
        final int rgb = Mth.hsvToRgb(hue, sat, val);
        final float r = ((rgb >> 16) & 0xFF) / 255f;
        final float g = ((rgb >> 8) & 0xFF) / 255f;
        final float b = (rgb & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, g, b, 1f);
        ctx.blit(ICONS, barX, barY, 0, 69, (int) (182 * meter), 5);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Text: tier label above bar
        final String tierRelease = meter >= 1.0f ? "§5Poison Frog Clouds" :
                                   meter >= 0.75f ? "§cLightning Storm Clouds" :
                                   meter >= 0.5f  ? "§bFreezing Winter Storm" :
                                   meter >= 0.25f ? "§fTwin Tornados" :
                                                    "§7(charge more to release)";
        ctx.drawCenteredString(minecraft.font, tierRelease, screenWidth / 2, barY - 10, 0xFFFFFF);
    }

    // Rendered using this mixin rather than HudRenderCallback, so it's behind chat.
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false),
            slice = @Slice(
                    from = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/scores/Scoreboard;getPlayersTeam(Ljava/lang/String;)Lnet/minecraft/world/scores/PlayerTeam;")))
    private void renderHud(GuiGraphics context, float tickDelta, CallbackInfo ci) {
        JCraftHudOverlay.render(context);
    }
}
