package net.arna.jcraft.common.worldgen;


import net.arna.jcraft.api.registry.JStructurePieceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ClusterTemplatePiece extends TemplateStructurePiece {
    private final ResourceLocation templateId;
    private final Rotation rotation;

    // Construct from a SinglePoolElement (which wraps a template id)
    public static ClusterTemplatePiece fromPoolElement(StructureTemplateManager mgr, SinglePoolElement elem, BlockPos pos, Rotation rot) {
        // SinglePoolElement stores the template location; in Mojang mappings it's accessible via "template" / fallback (check your mappings)
        ResourceLocation id = elem.template.left().orElseThrow(() -> new IllegalStateException("SinglePoolElement without template id"));
        return new ClusterTemplatePiece(mgr, id, pos, rot);
    }

    public ClusterTemplatePiece(StructureTemplateManager mgr, ResourceLocation templateId, BlockPos pos, Rotation rotation) {
        super(JStructurePieceRegistry.CLUSTER_PIECE.get(), 0, mgr, templateId, templateId.toString(), new StructurePlaceSettings().setRotation(rotation), pos);
        this.templateId = templateId;
        this.rotation = rotation;
    }

    // NBT load ctor
    public ClusterTemplatePiece(CompoundTag tag, StructureTemplateManager mgr) {
        super(JStructurePieceRegistry.CLUSTER_PIECE.get(), tag, mgr, rl -> new StructurePlaceSettings().setRotation(Rotation.valueOf(tag.getString("Rot"))));
        this.templateId = new ResourceLocation(tag.getString("Template"));
        this.rotation = Rotation.valueOf(tag.getString("Rot"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super.addAdditionalSaveData(ctx, tag);
        tag.putString("Template", this.templateId.toString());
        tag.putString("Rot", this.rotation.name());
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        // Handle structure block "data" markers if you use them inside the NBT
    }
}