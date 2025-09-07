package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.worldgen.ClusterTemplatePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public interface JStructurePieceRegistry {
    DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(JCraft.MOD_ID, Registries.STRUCTURE_PIECE);

    RegistrySupplier<StructurePieceType> CLUSTER_PIECE = STRUCTURE_PIECES.register(
            JCraft.id("cluster_pieces"),
            () -> (ctx, nbt) -> new ClusterTemplatePiece(nbt, ctx.structureTemplateManager())
    );

    static void register() {
        STRUCTURE_PIECES.register();
    }
}
