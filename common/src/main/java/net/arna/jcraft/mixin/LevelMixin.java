package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.events.JBlockEvents;
import net.arna.jcraft.mixin_logic.LevelAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAddon {

    private @Unique boolean jcraft$ignoreSetBlock = false;

    @WrapMethod(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    public boolean jcraft$fireBeforeSetEvent(final BlockPos pos, final BlockState newState, final int flags, final Operation<Boolean> original) {
        if (jcraft$ignoreSetBlock) {
            return original.call(pos, newState, flags);
        }
        final Level level = (Level)(Object)this;
        final BlockState oldState = level.getBlockState(pos);
        // don't notify no changes
        if (Objects.equals(oldState, newState)) {
            return original.call(pos, newState, flags);
        }

        // actually invoke the hook
        JBlockEvents.BEFORE_SET.invoker().setBlock(pos, oldState, newState, level);
        return original.call(pos, newState, flags);
    }

    // We don't fire the event in case of chunk generation (see LevelChunkMixin and MinecraftServerMixin)
    // as we don't need it there, and it'd fire a lot of events.
    @Override
    public void jcraft$setIgnoreSetBlock(boolean value) {
        jcraft$ignoreSetBlock = value;
    }
}
