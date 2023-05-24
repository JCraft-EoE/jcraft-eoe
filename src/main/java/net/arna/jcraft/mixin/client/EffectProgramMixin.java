package net.arna.jcraft.mixin.client;

import net.arna.jcraft.client.rendering.shader.JGLImportProcessor;
import net.minecraft.client.gl.EffectProgram;
import net.minecraft.client.gl.GLImportProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EffectProgram.class)
public class EffectProgramMixin {

    @ModifyArg(method = "createFromResource", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/EffectProgram;loadProgram(Lnet/minecraft/client/gl/Program$Type;Ljava/lang/String;Ljava/io/InputStream;Ljava/lang/String;Lnet/minecraft/client/gl/GLImportProcessor;)I"), index = 4)
    private static GLImportProcessor jcraft$useCustomPreprocessor(GLImportProcessor par5) {
        return new JGLImportProcessor();
    }
}
