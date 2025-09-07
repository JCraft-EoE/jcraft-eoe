package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.gui.hud.JCraftHudOverlay;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
