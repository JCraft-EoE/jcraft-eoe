package net.arna.jcraft.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.gui.hud.JCraftHudOverlay;
import net.arna.jcraft.common.component.JComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.arna.jcraft.client.gui.hud.JCraftAbilityHud.GUI_ICONS_TEXTURE;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin  {
    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private static final Identifier EMPTY_BLOOD_ICON = JCraft.id("textures/gui/blood_empty.png");
    @Unique
    private static final Identifier HALF_BLOOD_ICON = JCraft.id("textures/gui/blood_half.png");
    @Unique
    private static final Identifier FULL_BLOOD_ICON = JCraft.id("textures/gui/blood_full.png");

    @Redirect(
            method = "renderStatusBars",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/PlayerEntity;getAir()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    void showVampireBloodIcons(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height) {
        PlayerEntity player = client.player;
        if (JComponents.getVampirism(player).isVampire()) {
            instance.drawTexture(texture, x, y, 0, 0, width, height, 9, 9);
            RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        } else {
            instance.drawTexture(texture, x, y, u, v, width, height);
        }
    }

    @Inject(
            method = "renderStatusBars",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/PlayerEntity;getAir()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
                    ordinal = 0
            )
    )
    void switchToEmptyBloodIcon(DrawContext context, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (JComponents.getVampirism(player).isVampire()) {
            RenderSystem.setShaderTexture(0, EMPTY_BLOOD_ICON);
        }
    }
    @Inject(
            method = "renderStatusBars",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/PlayerEntity;getAir()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
                    ordinal = 2
            )
    )
    void switchToHalfBloodIcon(DrawContext context, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (JComponents.getVampirism(player).isVampire()) {
            RenderSystem.setShaderTexture(0, HALF_BLOOD_ICON);
        }
    }

    @Inject(
            method = "renderStatusBars",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE_ASSIGN",
                            target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/PlayerEntity;getAir()I"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
                    ordinal = 1
            )
    )
    void switchToFullBloodIcon(DrawContext context, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (JComponents.getVampirism(player).isVampire()) {
            RenderSystem.setShaderTexture(0, FULL_BLOOD_ICON);
        }
    }
    
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private void renderEpitaph(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (JClientConfig.getInstance().isEpitaphOverlay())
            EpitaphOverlay.render();
    }

    // Rendered using this mixin rather than HudRenderCallback, so it's behind chat.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getPlayerTeam(Ljava/lang/String;)Lnet/minecraft/scoreboard/Team;")))
    private void renderHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        JCraftHudOverlay.render(context);
    }
}
