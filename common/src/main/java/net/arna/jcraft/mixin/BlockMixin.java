package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    @ModifyReturnValue(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;", at = @At("RETURN"))
    private static List<ItemStack> jcraft$getDrops(final List<ItemStack> original, final BlockState state, final ServerLevel level, final BlockPos pos, final BlockEntity blockEntity) {
        final List<ItemStack> loot = new ArrayList<>(original);
        JBlockEvents.BEFORE_BLOCK_LOOT.invoker().processBlockLoot(loot, state, level, pos, blockEntity);
        return loot;
    }

    @ModifyReturnValue(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"))
    private static List<ItemStack> jcraft$getDrops(final List<ItemStack> original, final BlockState state, final ServerLevel level, final BlockPos pos, final BlockEntity blockEntity, final Entity entity, final ItemStack tool) {
        final List<ItemStack> loot = new ArrayList<>(original);
        JBlockEvents.BEFORE_BLOCK_LOOT.invoker().processBlockLoot(loot, state, level, pos, blockEntity);
        return loot;
    }

    @Inject(method = "wasExploded(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;)V", at = @At("RETURN"))
    public void jcraft$wasExploded(final Level level, final BlockPos pos, final Explosion explosion, final CallbackInfo ci) {
        JBlockEvents.AFTER_EXPLOSION.invoker().exploded(level, pos, explosion);
    }

}
