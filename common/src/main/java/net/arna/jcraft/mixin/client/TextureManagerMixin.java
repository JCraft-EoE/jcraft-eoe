package net.arna.jcraft.mixin.client;

import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void jcraft$tickAnimatedTextures(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && JClientUtils.isInTSRange(mc.player.position())) ci.cancel();
    }
}
