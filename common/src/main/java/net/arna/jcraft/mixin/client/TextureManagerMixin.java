package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    @WrapMethod(method = "tick")
    private void jcraft$tickAnimatedTextures(final Operation<Void> original) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !JClientUtils.isInTSRange(mc.player.position())) {
            original.call();
        }
    }

}
