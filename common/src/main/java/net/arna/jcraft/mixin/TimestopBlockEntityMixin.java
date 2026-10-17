package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.tickable.Timestops;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelChunk.class)
public abstract class TimestopBlockEntityMixin {

    @WrapMethod(method = "isTicking")
    boolean jcraft$canTickBlockEntity(final BlockPos pos, final Operation<Boolean> original) {
        if (Timestops.isInTSRange(pos)) {
            return false;
        }
        return original.call(pos);
    }

}