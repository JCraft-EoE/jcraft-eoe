package net.arna.jcraft.mixin;

import net.arna.jcraft.common.events.JBlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void jcraft$getDrops(final BlockState state, final ServerLevel level, final BlockPos pos, final BlockEntity blockEntity, final CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> loot = cir.getReturnValue();
        if (!JBlockEvents.BEFORE_BLOCK_LOOT.invoker().processBlockLoot(loot, state, level, pos, blockEntity).interruptsFurtherEvaluation()) {
            cir.setReturnValue(loot);
        }
    }

    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void jcraft$getDrops(final BlockState state, final ServerLevel level, final BlockPos pos, final BlockEntity blockEntity, final Entity entity, final ItemStack tool, final CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> loot = cir.getReturnValue();
        if (!JBlockEvents.BEFORE_BLOCK_LOOT.invoker().processBlockLoot(loot, state, level, pos, blockEntity).interruptsFurtherEvaluation()) {
            cir.setReturnValue(loot);
        }
    }

    @Inject(method = "Lnet/minecraft/world/level/block/Block;wasExploded(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;)V", at = @At("RETURN"))
    public void jcraft$wasExploded(final Level level, final BlockPos pos, final Explosion explosion, final CallbackInfo ci) {
        JBlockEvents.AFTER_EXPLOSION.invoker().exploded(level, pos, explosion);
    }
}
