package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.rendering.shader.JGLImportProcessor;
import net.minecraft.client.gl.EffectShaderStage;
import net.minecraft.client.gl.GlImportProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EffectShaderStage.class)
public class EffectProgramMixin {

    @ModifyArg(method = "createFromResource", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gl/EffectShaderStage;load(Lnet/minecraft/client/gl/ShaderStage$Type;Ljava/lang/String;Ljava/io/InputStream;Ljava/lang/String;Lnet/minecraft/client/gl/GlImportProcessor;)I"
    ), index = 4)
    private static GlImportProcessor jcraft$useCustomPreprocessor(GlImportProcessor par5) {
        return new JGLImportProcessor();
    }
}
