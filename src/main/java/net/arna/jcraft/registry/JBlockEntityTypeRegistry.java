package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.entity.ShaderTestBlockEntity;
import net.arna.jcraft.common.block.tile.CoffinTileEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface JBlockEntityTypeRegistry {
    Map<Identifier, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new LinkedHashMap<>();

    BlockEntityType<ShaderTestBlockEntity> SHADER_TEST_BLOCK_ENTITY = register("beam",
            FabricBlockEntityTypeBuilder.create(ShaderTestBlockEntity::new, JObjectRegistry.SHADER_TEST_BLOCK).build(null));
    BlockEntityType<CoffinTileEntity> COFFIN_TILE = register("coffin_tile",
            FabricBlockEntityTypeBuilder.create(CoffinTileEntity::new, JObjectRegistry.COFFIN_BLOCK).build(null));

    static <T extends BlockEntity> BlockEntityType<T> register(String id, BlockEntityType<T> type) {
        BLOCK_ENTITY_TYPES.put(JCraft.id(id), type);
        return type;
    }

    static void init() {
        BLOCK_ENTITY_TYPES.forEach((id, entityType) -> Registry.register(Registries.BLOCK_ENTITY_TYPE, id, entityType));
    }
}
