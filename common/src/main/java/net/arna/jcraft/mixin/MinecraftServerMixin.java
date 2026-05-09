package net.arna.jcraft.mixin;

import net.arna.jcraft.mixin_logic.LevelAddon;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "prepareLevels", at = @At("HEAD"))
    private void disableSetBlockEvent(ChunkProgressListener listener, CallbackInfo ci) {
        for (ServerLevel level : levels.values()) {
            ((LevelAddon) level).jcraft$setIgnoreSetBlock(true);
        }
    }

    @Inject(method = "prepareLevels", at = @At("RETURN"))
    private void enableSetBlockEvent(ChunkProgressListener listener, CallbackInfo ci) {
        for (ServerLevel level : levels.values()) {
            ((LevelAddon) level).jcraft$setIgnoreSetBlock(false);
        }
    }
}
