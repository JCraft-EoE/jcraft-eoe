package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.List;

import static net.arna.jcraft.JCraft.PARTICLES;

public interface JParticleTypeRegistry {

    RegistrySupplier<SimpleParticleType> AURA_ARC = PARTICLES.register("aura_arc", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> COMBO_BREAK = PARTICLES.register("combo_break", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> COOLDOWN_CANCEL = PARTICLES.register("cooldown_cancel", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> HITSPARK_1 = PARTICLES.register("hitspark_1", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> HITSPARK_2 = PARTICLES.register("hitspark_2", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> HITSPARK_3 = PARTICLES.register("hitspark_3", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> INVERTED_HITSPARK_3 = PARTICLES.register("inverted_hitspark_3", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> STUN_SLASH = PARTICLES.register("stun_slash", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> STUN_PIERCE = PARTICLES.register("stun_pierce", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> AURA_BLOB = PARTICLES.register("aura_blob", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> KCPARTICLE = PARTICLES.register("kcparticle", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> BACKSTAB = PARTICLES.register("backstab", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> SPEED_PARTICLE = PARTICLES.register("speedparticle", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> BITES_THE_DUST = PARTICLES.register("btd", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> BOOM_1 = PARTICLES.register("boom_1", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> PIXEL = PARTICLES.register("pixel", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> BLOCKSPARK = PARTICLES.register("blockspark", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> GO = PARTICLES.register("go", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> DO = PARTICLES.register("do", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> INVERSION = PARTICLES.register("inversion", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> OVERLAP = PARTICLES.register("overlap", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> SUN_LOCK_ON = PARTICLES.register("sun_lock_on", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> PURPLE_HAZE_CLOUD = PARTICLES.register("purple_haze_cloud", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> PURPLE_HAZE_PARTICLE = PARTICLES.register("purple_haze_particle", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> DAMAGE_NUMBER = PARTICLES.register("damage_number", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> HAMON_SPARK = PARTICLES.register("hamon_spark", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> LEMON = PARTICLES.register("lemon", () -> new SimpleParticleType(false));

    // Metallica mosh
    RegistrySupplier<SimpleParticleType> METALLICA_MOSH_1 = PARTICLES.register("metallica_mosh_1", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> METALLICA_MOSH_2 = PARTICLES.register("metallica_mosh_2", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> METALLICA_MOSH_3 = PARTICLES.register("metallica_mosh_3", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> METALLICA_MOSH_4 = PARTICLES.register("metallica_mosh_4", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> METALLICA_MOSH_5 = PARTICLES.register("metallica_mosh_5", () -> new SimpleParticleType(false));
    List<RegistrySupplier<SimpleParticleType>> MOSH_TYPES = List.of(METALLICA_MOSH_1, METALLICA_MOSH_2, METALLICA_MOSH_3, METALLICA_MOSH_4, METALLICA_MOSH_5);

    static void init() {
        // intentionally left empty
    }
}
