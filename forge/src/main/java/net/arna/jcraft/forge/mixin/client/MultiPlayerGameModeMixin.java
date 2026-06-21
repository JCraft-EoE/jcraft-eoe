package net.arna.jcraft.forge.mixin.client;

import net.arna.jcraft.client.util.BlockBreakerClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow
    private float destroyProgress;

    @Inject(method = "lambda$startDestroyBlock$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void setDestroyProgressOnBreakStart(BlockState blockState, PlayerInteractEvent.LeftClickBlock event,
                                                BlockPos blockPos, Direction direction, int i,
                                                CallbackInfoReturnable<Packet<ServerGamePacketListener>> cir) {
        int progress = BlockBreakerClient.getBreakProgress(blockPos);

        // When the player starts breaking a block, check if the block is already partially broken.
        // If so, we start off there rather than breaking from 0.
        // The server must agree with this. See ServerPlayerGameModeMixin.
        if (progress > 0) destroyProgress = progress / 10f;
    }
}
