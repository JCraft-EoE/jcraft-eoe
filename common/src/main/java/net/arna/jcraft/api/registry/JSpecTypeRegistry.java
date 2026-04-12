package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.arna.jcraft.common.spec.BrawlerSpec;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.spec.VampireSpec;
import net.minecraft.Util;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public interface JSpecTypeRegistry {
    DeferredRegister<SpecType> SPEC_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.SPEC_TYPE_REGISTRY_KEY);

    RegistrySupplier<SpecType> NONE = register("none", user -> null);
    RegistrySupplier<SpecType> BRAWLER = register("brawler", BrawlerSpec::new);
    RegistrySupplier<SpecType> ANUBIS = register("anubis", AnubisSpec::new);
    RegistrySupplier<SpecType> VAMPIRE = register("vampire", VampireSpec::new);
    RegistrySupplier<SpecType> HAMON = register("hamon", HamonSpec::new);

    Int2ObjectMap<RegistrySupplier<SpecType>> LEGACY_ORDINALS = Util.make(new Int2ObjectArrayMap<>(), map -> {
        map.put(0, NONE);
        map.put(1, BRAWLER);
        map.put(2, ANUBIS);
        map.put(3, VAMPIRE);
    });

    private static RegistrySupplier<SpecType> register(String name, Function<LivingEntity, JSpec<?,?>> factory) {
        return SPEC_TYPE_REGISTRY.register(name, () -> SpecType.of(JCraft.id(name), factory));
    }
}
