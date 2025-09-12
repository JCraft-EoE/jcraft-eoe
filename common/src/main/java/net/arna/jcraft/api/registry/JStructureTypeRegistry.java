package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.worldgen.ClusterStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;

public interface JStructureTypeRegistry {
    DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(JCraft.MOD_ID, Registries.STRUCTURE_TYPE);

    RegistrySupplier<StructureType<ClusterStructure>> CLUSTER = STRUCTURE_TYPES.register(
            JCraft.id("cluster"),
            () -> ClusterStructure.CODEC::codec
    );

    static void register() {
        STRUCTURE_TYPES.register();
    }
}
