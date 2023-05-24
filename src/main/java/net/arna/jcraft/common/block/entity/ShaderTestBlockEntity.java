package net.arna.jcraft.common.block.entity;

import net.arna.jcraft.registry.JBlockEntityTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ShaderTestBlockEntity extends BlockEntity {
    public ShaderTestBlockEntity(BlockPos pos, BlockState state) {
        super(JBlockEntityTypeRegistry.SHADER_TEST_BLOCK_ENTITY, pos, state);
    }
}
