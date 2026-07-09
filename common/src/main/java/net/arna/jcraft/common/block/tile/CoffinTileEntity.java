package net.arna.jcraft.common.block.tile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.api.registry.JBlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CoffinTileEntity extends BlockEntity {

    public static final AzCommand OPEN = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.coffin.open", AzPlayBehaviors.LOOP);
    public static final AzCommand CLOSE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.coffin.closed", AzPlayBehaviors.LOOP);

    public CoffinTileEntity(final BlockPos pos, final BlockState state) {
        super(JBlockEntityTypeRegistry.COFFIN_TILE.get(), pos, state);
    }

    public static void tickClient(final @NonNull Level level, final @NonNull BlockPos pos, final @NonNull BlockState state, final @NonNull CoffinTileEntity blockEntity) {
        if (state.getValue(CoffinBlock.OCCUPIED)) {
            CLOSE.sendForBlockEntity(blockEntity);
        }
        else {
            OPEN.sendForBlockEntity(blockEntity);
        }
    }

}
