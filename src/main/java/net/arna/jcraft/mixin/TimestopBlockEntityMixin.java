package net.arna.jcraft.mixin;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class TimestopBlockEntityMixin {
    @SuppressWarnings("CancellableInjectionUsage") // The warning is flat out wrong
    @Inject(method = "canTickBlockEntity", at = @At("HEAD"), cancellable = true)
    void jcraft$canTickBlockEntity(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (JUtils.isInTSRange(pos)) cir.cancel();
    }
}