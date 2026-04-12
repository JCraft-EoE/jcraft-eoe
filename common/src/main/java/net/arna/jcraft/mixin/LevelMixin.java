package net.arna.jcraft.mixin;

import net.arna.jcraft.common.events.JBlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = Level.class)
public class LevelMixin {

    @Unique
    private static final String MINECRAFT_SERVER_NAME = MinecraftServer.class.getName();
    @Unique
    private static final String LEVEL_CHUNK_NAME = LevelChunk.class.getName();

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"), cancellable = true)
    public void jcraft$setBlock(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        final Level level = (Level)(Object)this;
        // only on server side
        if (!level.isClientSide()) {
            final BlockState oldState = level.getBlockState(pos);
            // don't notify no changes
            if (Objects.equals(oldState, newState)) {
                return;
            }
            // avoid chunk generation
            // todo: pliz make the mixin itself just not apply to a select few classes
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if ((MINECRAFT_SERVER_NAME.equals(element.getClassName()) && "prepareLevels".equals(element.getMethodName())) ||
                        LEVEL_CHUNK_NAME.equals(element.getClassName()) && "postProcessGeneration".equals(element.getMethodName())) {
                    return;
                }
            }
            // actually invoke the hook
            if (JBlockEvents.BEFORE_SET.invoker().setBlock(pos, oldState, newState, level).interruptsFurtherEvaluation()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

}
