package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public interface JParticleTypeRegistry {


    DefaultParticleType COMBO_BREAK = FabricParticleTypes.simple();
    DefaultParticleType COOLDOWN_CANCEL = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_1 = FabricParticleTypes.simple();
    DefaultParticleType HITSPARK_2 = FabricParticleTypes.simple();
    DefaultParticleType KCPARTICLE = FabricParticleTypes.simple();
    DefaultParticleType BACKSTAB = FabricParticleTypes.simple();
    DefaultParticleType SPEEDPARTICLE = FabricParticleTypes.simple();
    DefaultParticleType BITES_THE_DUST = FabricParticleTypes.simple();

    Map<Integer, DefaultParticleType> particles = Map.ofEntries(
            Map.entry(-4, JParticleTypeRegistry.BITES_THE_DUST),
            Map.entry(-3, ParticleTypes.SWEEP_ATTACK),
            Map.entry(-2, BACKSTAB),
            Map.entry(-1, ParticleTypes.FLASH),
            Map.entry(0, JParticleTypeRegistry.COMBO_BREAK),
            Map.entry(1, JParticleTypeRegistry.COOLDOWN_CANCEL),
            Map.entry(2, JParticleTypeRegistry.HITSPARK_1),
            Map.entry(3, JParticleTypeRegistry.HITSPARK_2)
    );

    static void initParticleTypes() {
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("combo_break"), COMBO_BREAK);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("cooldown_cancel"), COOLDOWN_CANCEL);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("hitspark_1"), HITSPARK_1);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("hitspark_2"), HITSPARK_2);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("kcparticle"), KCPARTICLE);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("backstab"), BACKSTAB);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("speedparticle"), SPEEDPARTICLE);
        Registry.register(Registry.PARTICLE_TYPE, JCraft.id("btd"), BITES_THE_DUST);
    }
}
