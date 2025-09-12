package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.SimpleParticleType;

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
    RegistrySupplier<SimpleParticleType> INVERSION = PARTICLES.register("inversion", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> SUN_LOCK_ON = PARTICLES.register("sun_lock_on", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> PURPLE_HAZE_CLOUD = PARTICLES.register("purple_haze_cloud", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> PURPLE_HAZE_PARTICLE = PARTICLES.register("purple_haze_particle", () -> new SimpleParticleType(false));
    RegistrySupplier<SimpleParticleType> DAMAGE_NUMBER = PARTICLES.register("damage_number", () -> new SimpleParticleType(false));

    /*
    TODO: impl this properly (fuck forge)

    record ParticleData <T extends ParticleType<T> & ParticleOptions> (
            RegistrySupplier<? extends ParticleType<T>> supplier,
            ParticleProviderRegistry.DeferredParticleProvider<T> provider) {
    }

    ParticleData<?>[] entries = {
            new ParticleData<>(JParticleTypeRegistry.COMBO_BREAK, ComboBreakerParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.COOLDOWN_CANCEL, CooldownCancelParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.HITSPARK_1, provider -> new HitsparkParticle.Factory(provider, 0.4f, 5)),
            new ParticleData<>(JParticleTypeRegistry.HITSPARK_2, provider -> new HitsparkParticle.Factory(provider, 0.66f, 6)),
            new ParticleData<>(JParticleTypeRegistry.HITSPARK_3, provider -> new HitsparkParticle.Factory(provider, 1f, 8)),
            new ParticleData<>(JParticleTypeRegistry.INVERTED_HITSPARK_3, provider -> new InvertedHitsparkParticle.Factory(provider, 1f, 8)),
            new ParticleData<>(JParticleTypeRegistry.STUN_SLASH, provider -> new HitsparkParticle.Factory(provider, 0.6f, 6)),
            new ParticleData<>(JParticleTypeRegistry.STUN_PIERCE, provider -> new HitsparkParticle.Factory(provider, 0.6f, 6)),
            new ParticleData<>(JParticleTypeRegistry.KCPARTICLE, KCParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.BACKSTAB, BackstabParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.SPEED_PARTICLE, SpeedParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.BITES_THE_DUST, BitesTheDustParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.BOOM_1, BoomParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.PIXEL, PixelParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.BLOCKSPARK, provider -> new BlocksparkParticle.Factory(provider, 0.15f)),
            new ParticleData<>(JParticleTypeRegistry.GO, GoParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.AURA_ARC, AuraArcParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.AURA_BLOB, AuraBlobParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.INVERSION, InversionParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.SUN_LOCK_ON, BackstabParticle.Factory::new), // 9 frames, reusing
            new ParticleData<>(JParticleTypeRegistry.PURPLE_HAZE_CLOUD, PurpleHazeCloudParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.PURPLE_HAZE_PARTICLE, PurpleHazeErraticParticle.Factory::new),
            new ParticleData<>(JParticleTypeRegistry.DAMAGE_NUMBER, DamageNumberParticle.Factory::new),
    };
     */

    static void init() {
        // intentionally left empty
    }
}
