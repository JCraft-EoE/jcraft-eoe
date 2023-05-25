package net.arna.jcraft.common.block;

import net.arna.jcraft.common.block.entity.ShaderTestBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public class ShaderTestBlock extends BlockWithEntity {
    public ShaderTestBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShaderTestBlockEntity(pos, state);
    }
}
