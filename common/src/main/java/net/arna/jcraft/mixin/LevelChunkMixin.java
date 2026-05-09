package net.arna.jcraft.mixin;

import net.arna.jcraft.mixin_logic.LevelAddon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Shadow
    @Final
    Level level;

    @Inject(method = "postProcessGeneration", at = @At("HEAD"))
    private void disableSetBlockEvent(CallbackInfo ci) {
        ((LevelAddon) level).jcraft$setIgnoreSetBlock(true);
    }

    @Inject(method = "postProcessGeneration", at = @At("RETURN"))
    private void enableSetBlockEvent(CallbackInfo ci) {
        ((LevelAddon) level).jcraft$setIgnoreSetBlock(false);
    }
}
