package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.hud.EpitaphOverlay;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private void renderEpitaph(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
//        if (JConfig.EPITAPH_OVERLAY)
            EpitaphOverlay.render();
    }
}
