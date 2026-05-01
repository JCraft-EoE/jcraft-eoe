package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.splatter.SplatterFactory;
import net.arna.jcraft.common.splatter.AcidSplatter;
import net.arna.jcraft.common.splatter.BloodSplatter;
import net.arna.jcraft.common.splatter.GasolineSplatter;

public interface JSplatterTypeRegistry {
    DeferredRegister<SplatterFactory> SPLATTER_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.SPLATTER_TYPE_REGISTRY_KEY);

    RegistrySupplier<SplatterFactory> BLOOD_SPLATTER_TYPE = SPLATTER_TYPE_REGISTRY.register("blood", () -> BloodSplatter::new);
    RegistrySupplier<SplatterFactory> ACID_SPLATTER_TYPE = SPLATTER_TYPE_REGISTRY.register("acid", () -> AcidSplatter::new);
    RegistrySupplier<SplatterFactory> GASOLINE_SPLATTER_TYPE = SPLATTER_TYPE_REGISTRY.register("gasoline", () -> GasolineSplatter::new);
}
