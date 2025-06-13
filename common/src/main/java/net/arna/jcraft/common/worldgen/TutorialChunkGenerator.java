package net.arna.jcraft.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JBiomeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class TutorialChunkGenerator extends ChunkGenerator {
    public static final Codec<TutorialChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryOps.retrieveElement(JBiomeRegistry.TUTORIAL), Codec.BOOL.fieldOf("dimensional").forGetter(TutorialChunkGenerator::isDimensional)).apply(instance, instance.stable(TutorialChunkGenerator::new)));

    public boolean dimensional;

    public TutorialChunkGenerator(final Holder.Reference<Biome> biome, final boolean dimensional) {
        super(new FixedBiomeSource(biome));
        this.dimensional = dimensional;
    }

    public boolean isDimensional() {
        return dimensional;
    }

    @NotNull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(final @NotNull WorldGenRegion level, final long seed, final @NotNull RandomState random, final @NotNull BiomeManager biomeManager, final @NotNull StructureManager structureManager, final @NotNull ChunkAccess chunk, final @NotNull GenerationStep.Carving step) {
        /* Empty on purpose */
    }

    @Override
    public void buildSurface(final @NotNull WorldGenRegion level, final @NotNull StructureManager structureManager, final @NotNull RandomState random, final @NotNull ChunkAccess chunk) {
        if (chunk.getPos().x % 3 != 0 || chunk.getPos().z % 3 != 0) {
            return;
        }
        final Structure tutorialStructure = level.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE).get(JCraft.id("tutorial"));
        final StructureTemplateManager structureTemplateManager = level.getServer().getStructureManager();
        final StructureStart structureStart = tutorialStructure.generate(
                level.registryAccess(),
                this,
                this.getBiomeSource(),
                random,
                structureTemplateManager,
                level.getSeed(),
                chunk.getPos(),
                0,
                level,
                (holder) -> true);
        if (structureStart.isValid()) {
            final ChunkPos endPos = new ChunkPos(chunk.getPos().x + 2, chunk.getPos().z + 2);
            ChunkPos.rangeClosed(chunk.getPos(), endPos).forEach(
                    (chunkPos) -> structureStart.placeInChunk(
                            level,
                            structureManager,
                            this,
                            level.getRandom(),
                            new BoundingBox(
                                    chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(),
                                    chunkPos.getMaxBlockX(), 48, chunkPos.getMaxBlockZ()
                            ),
                            chunkPos
                    )
            );
        }
    }

    @Override
    public void spawnOriginalMobs(final @NotNull WorldGenRegion level) {
        /* Empty on purpose */
    }

    @Override
    public int getGenDepth() {
        return 320;
    }

    @NotNull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final @NotNull Executor executor, final @NotNull Blender blender, final @NotNull RandomState random, final @NotNull StructureManager structureManager, final @NotNull ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 7;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(final int x, final int z, final @NotNull Heightmap.Types type, final @NotNull LevelHeightAccessor level, final @NotNull RandomState random) {
        return 0;
    }

    @NotNull
    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor height, final @NotNull RandomState random) {
        final BlockState[] column = new BlockState[height.getHeight()];
        for (int i = 1; i < column.length; i++) {
            column[i] = Blocks.AIR.defaultBlockState();
        }
        if (column.length > 0) {
            column[0] = Blocks.BEDROCK.defaultBlockState();
        }
        return new NoiseColumn(height.getMinBuildHeight(), column);
    }

    @Override
    public void addDebugScreenInfo(final @NotNull List<String> info, final @NotNull RandomState random, final @NotNull BlockPos pos) {
        /* Empty on purpose */
    }
}
