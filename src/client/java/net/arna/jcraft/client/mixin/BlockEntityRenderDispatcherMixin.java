package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    //todo: disable deltaTick for blocks caught in timestop
    private void jcraft$deltaTickBlock(Args args) {
        BlockEntity blockEntity = args.get(0);
        if (JClientUtils.getTicksIfInTSRange(blockEntity.getPos()) > 0) args.set(1, 0.0F); // Args 0 = blockent, 1 = deltatick
    }
}
