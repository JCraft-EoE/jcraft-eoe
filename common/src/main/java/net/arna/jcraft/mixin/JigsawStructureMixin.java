package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(JigsawStructure.class)
public abstract class JigsawStructureMixin extends StructureMixin {

    @Shadow
    @Final
    private Holder<StructureTemplatePool> startPool;

    @Override
    protected void jcraft$overrideForJigsaw(final WorldGenLevel level, final StructureManager structureManager, final ChunkGenerator chunkGenerator, final RandomSource random, final BoundingBox boundingBox, final ChunkPos chunkPos, final PiecesContainer pieces, final Operation<Void> original) {
        if (!startPool.is(JTagRegistry.STONE_BASE)) {
            return;
        }

        // copied from WoodlandMansionStructure
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i = level.getMinBuildHeight();
        BoundingBox boundingBox2 = pieces.calculateBoundingBox();
        int j = boundingBox2.minY();

        for(int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
            for (int l = boundingBox.minZ(); l <= boundingBox.maxZ(); ++l) {
                mutableBlockPos.set(k, j, l);
                if (!level.isEmptyBlock(mutableBlockPos) && boundingBox2.isInside(mutableBlockPos) && pieces.isInsidePiece(mutableBlockPos)) {
                    for (int m = j - 1; m > i; --m) {
                        mutableBlockPos.setY(m);
                        if (!level.isEmptyBlock(mutableBlockPos) && !level.getBlockState(mutableBlockPos).liquid()) {
                            break;
                        }

                        level.setBlock(mutableBlockPos, Blocks.STONE.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
