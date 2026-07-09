package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Structure.class)
public abstract class StructureMixin {
    @WrapMethod(method = "afterPlace")
    protected void jcraft$overrideForJigsaw(final WorldGenLevel level, final StructureManager structureManager, final ChunkGenerator chunkGenerator, final RandomSource random, final BoundingBox boundingBox, final ChunkPos chunkPos, final PiecesContainer pieces, final Operation<Void> original) {
        // intentionally left empty
    }
}
