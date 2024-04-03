package net.arna.jcraft.mixin;

import net.arna.jcraft.common.tickable.Timestops;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.TickPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldAccess.class)
public interface TimestopBlockMixin {
    @Inject(method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;ILnet/minecraft/world/tick/TickPriority;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$createAndScheduleBlockTick(BlockPos pos, Block block, int delay, TickPriority priority, CallbackInfo info) {
        int ticks = Timestops.getTicksIfInTSRange(pos);

        if (ticks > 0) {
            WorldAccess worldAccess = (WorldAccess) this;
            worldAccess.getBlockTickScheduler().scheduleTick(
                    new OrderedTick<>(block, pos, worldAccess.getLevelProperties().getTime() + (long) ticks + delay, priority, worldAccess.getTickOrder())
            );
            info.cancel();
        }
    }

    @Inject(method = "scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$createAndScheduleBlockTick(BlockPos pos, Block block, int delay, CallbackInfo info) {
        int ticks = Timestops.getTicksIfInTSRange(pos);

        if (ticks > 0) {
            WorldAccess worldAccess = (WorldAccess) this;
            worldAccess.getBlockTickScheduler().scheduleTick(
                    new OrderedTick<>(block, pos, worldAccess.getLevelProperties().getTime() + (long) ticks + delay, worldAccess.getTickOrder())
            );
            info.cancel();
        }
    }

    @Inject(method = "scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;ILnet/minecraft/world/tick/TickPriority;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$createAndScheduleFluidTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority, CallbackInfo info) {
        int ticks = Timestops.getTicksIfInTSRange(pos);

        if (ticks > 0) {
            WorldAccess worldAccess = (WorldAccess) this;
            worldAccess.getFluidTickScheduler().scheduleTick(
                    new OrderedTick<>(fluid, pos, worldAccess.getLevelProperties().getTime() + (long) ticks + delay, priority, worldAccess.getTickOrder())
            );
            info.cancel();
        }
    }

    @Inject(method = "scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;I)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$createAndScheduleFluidTick(BlockPos pos, Fluid fluid, int delay, CallbackInfo info) {
        int ticks = Timestops.getTicksIfInTSRange(pos);

        if (ticks > 0) {
            WorldAccess worldAccess = (WorldAccess) this;
            worldAccess.getFluidTickScheduler().scheduleTick(
                    new OrderedTick<>(fluid, pos, worldAccess.getLevelProperties().getTime() + (long) ticks + delay, worldAccess.getTickOrder())
            );
            info.cancel();
        }
    }
}