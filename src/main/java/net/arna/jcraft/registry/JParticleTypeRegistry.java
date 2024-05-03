package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface JParticleTypeRegistry {
    DefaultParticleType AURA_ARC = FabricParticleTypes.simple();
    DefaultParticleType AURA_BLOB = FabricParticleTypes.simple();
    DefaultParticleType COMBO_BREAK = FabricParticleTypes.simple();
    DefaultParticleType COOLDOWN_CANCEL = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_1 = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_2 = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_3 = FabricParticleTypes.simple();
    DefaultParticleType KCPARTICLE = FabricParticleTypes.simple();
    DefaultParticleType BACKSTAB = FabricParticleTypes.simple();
    DefaultParticleType SPEED_PARTICLE = FabricParticleTypes.simple();
    DefaultParticleType BITES_THE_DUST = FabricParticleTypes.simple();
    DefaultParticleType BOOM_1 = FabricParticleTypes.simple();
    DefaultParticleType PIXEL = FabricParticleTypes.simple();
    DefaultParticleType BLOCKSPARK = FabricParticleTypes.simple();
    DefaultParticleType GO = FabricParticleTypes.simple();
    DefaultParticleType INVERSION = FabricParticleTypes.simple();
    DefaultParticleType SUN_LOCK_ON = FabricParticleTypes.simple();
    DefaultParticleType PURPLE_HAZE_CLOUD = FabricParticleTypes.simple();
    DefaultParticleType PURPLE_HAZE_PARTICLE = FabricParticleTypes.simple();

    private static void registerParticle(String identifier, ParticleType<?> type) {
        Registry.register(Registries.PARTICLE_TYPE, JCraft.id(identifier), type);
    }

    static void initParticleTypes() {
        registerParticle("combo_break", COMBO_BREAK);
        registerParticle("cooldown_cancel", COOLDOWN_CANCEL);
        registerParticle("hitspark_1", HITSPARK_1);
        registerParticle("hitspark_2", HITSPARK_2);
        registerParticle("hitspark_3", HITSPARK_3);
        registerParticle("kcparticle", KCPARTICLE);
        registerParticle("backstab", BACKSTAB);
        registerParticle("speedparticle", SPEED_PARTICLE);
        registerParticle("btd", BITES_THE_DUST);
        registerParticle("boom_1", BOOM_1);
        registerParticle("pixel", PIXEL);
        registerParticle("blockspark", BLOCKSPARK);
        registerParticle("go", GO);
        registerParticle("aura_arc", AURA_ARC);
        registerParticle("aura_blob", AURA_BLOB);
        registerParticle("inversion", INVERSION);
        registerParticle("sun_lock_on", SUN_LOCK_ON);
        registerParticle("purple_haze_cloud", PURPLE_HAZE_CLOUD);
        registerParticle("purple_haze_particle", PURPLE_HAZE_PARTICLE);
    }
}
