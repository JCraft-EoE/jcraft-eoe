package net.arna.jcraft.mixin;

import net.arna.jcraft.common.events.JBlockEvents;
import net.arna.jcraft.mixin_logic.LevelAddon;
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

@Mixin(Level.class)
public class LevelMixin implements LevelAddon {
    private @Unique boolean ignoreSetBlock = false;

    @Unique
    private static final String MINECRAFT_SERVER_NAME = MinecraftServer.class.getName();
    @Unique
    private static final String LEVEL_CHUNK_NAME = LevelChunk.class.getName();

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"), cancellable = true)
    public void jcraft$fireBeforeSetEvent(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (ignoreSetBlock) {
            return;
        }

        final Level level = (Level)(Object)this;
        final BlockState oldState = level.getBlockState(pos);
        // don't notify no changes
        if (Objects.equals(oldState, newState)) {
            return;
        }

        // actually invoke the hook
        if (JBlockEvents.BEFORE_SET.invoker().setBlock(pos, oldState, newState, level).interruptsFurtherEvaluation()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    // We don't fire the event in case of chunk generation (see LevelChunkMixin and MinecraftServerMixin)
    // as we don't need it there, and it'd fire a lot of events.
    @Override
    public void jcraft$setIgnoreSetBlock(boolean value) {
        ignoreSetBlock = value;
    }
}
