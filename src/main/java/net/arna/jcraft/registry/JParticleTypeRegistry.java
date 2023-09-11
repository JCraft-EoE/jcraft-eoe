package net.arna.jcraft.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public interface JParticleTypeRegistry {

    DefaultParticleType COMBO_BREAK = FabricParticleTypes.simple();
    DefaultParticleType COOLDOWN_CANCEL = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_1 = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_2 = FabricParticleTypes.simple();
    DefaultParticleType KCPARTICLE = FabricParticleTypes.simple();
    DefaultParticleType BACKSTAB = FabricParticleTypes.simple();
    DefaultParticleType SPEED_PARTICLE = FabricParticleTypes.simple();
    DefaultParticleType BITES_THE_DUST = FabricParticleTypes.simple();
    DefaultParticleType BOOM_1 = FabricParticleTypes.simple();

    Int2ObjectMap<DefaultParticleType> particles = new Int2ObjectOpenHashMap<>(Map.ofEntries(
            Map.entry(-5, JParticleTypeRegistry.BOOM_1),
            Map.entry(-4, JParticleTypeRegistry.BITES_THE_DUST),
            Map.entry(-3, ParticleTypes.SWEEP_ATTACK),
            Map.entry(-2, BACKSTAB),
            Map.entry(-1, ParticleTypes.FLASH),
            Map.entry(0, JParticleTypeRegistry.COMBO_BREAK),
            Map.entry(1, JParticleTypeRegistry.COOLDOWN_CANCEL),
            Map.entry(2, JParticleTypeRegistry.HITSPARK_1),
            Map.entry(3, JParticleTypeRegistry.HITSPARK_2)
    ));

    private static void registerParticle(String identifier, DefaultParticleType type) {
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id(identifier), type);
    }

    static void initParticleTypes() {
        registerParticle("combo_break", COMBO_BREAK);
        registerParticle("cooldown_cancel", COOLDOWN_CANCEL);
        registerParticle("hitspark_1", HITSPARK_1);
        registerParticle("hitspark_2", HITSPARK_2);
        registerParticle("kcparticle", KCPARTICLE);
        registerParticle("backstab", BACKSTAB);
        registerParticle("speedparticle", SPEED_PARTICLE);
        registerParticle("btd", BITES_THE_DUST);
        registerParticle("boom_1", BOOM_1);
    }
}
