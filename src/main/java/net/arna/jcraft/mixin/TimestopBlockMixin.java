package net.arna.jcraft.mixin;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.OrderedTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldAccess.class)
public interface TimestopBlockMixin {
    @Inject(method = "createAndScheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/TickPriority;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$createAndScheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority, CallbackInfo info) {
        int ticks = JUtils.getTicksIfInTSRange(pos);

        if (ticks > 0) {
            WorldAccess worldAccess = (WorldAccess) this;
            worldAccess.getBlockTickScheduler().scheduleTick(
                    new OrderedTick<>(block, pos, worldAccess.getLevelProperties().getTime() + (long) ticks + delay, priority, worldAccess.getTickOrder())
            );
            info.cancel();
        }
    }
}