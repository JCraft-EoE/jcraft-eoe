package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.hud.EpitaphOverlay;
import net.arna.jcraft.client.hud.JCraftHudOverlay;
import net.arna.jcraft.common.JConfig;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private void renderEpitaph(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (JConfig.EPITAPH_OVERLAY)
            EpitaphOverlay.render();
    }

    // Rendered using this mixin rather than HudRenderCallback, so it's behind chat.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getPlayerTeam(Ljava/lang/String;)Lnet/minecraft/scoreboard/Team;")))
    private void renderHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        JCraftHudOverlay.render(matrices);
    }
}
