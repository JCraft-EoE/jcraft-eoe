package net.arna.jcraft.common.util;

import lombok.Getter;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;

@Getter
public enum JParticleType {
    BOOM(JParticleTypeRegistry.BOOM_1),
    BITES_THE_DUST(JParticleTypeRegistry.BITES_THE_DUST),
    SWEEP_ATTACK(ParticleTypes.SWEEP_ATTACK),
    BACK_STAB(JParticleTypeRegistry.BACKSTAB),
    FLASH(ParticleTypes.FLASH),
    COMBO_BREAK(JParticleTypeRegistry.COMBO_BREAK),
    COOLDOWN_CANCEL(JParticleTypeRegistry.COOLDOWN_CANCEL),
    HIT_SPARK_1(JParticleTypeRegistry.HITSPARK_1),
    HIT_SPARK_2(JParticleTypeRegistry.HITSPARK_2),
    HIT_SPARK_3(JParticleTypeRegistry.HITSPARK_3),
    PIXEL(JParticleTypeRegistry.PIXEL),
    BLOCK_SPARK(JParticleTypeRegistry.BLOCKSPARK),
    GO(JParticleTypeRegistry.GO),
    AURA_ARC(JParticleTypeRegistry.AURA_ARC),
    AURA_BLOB(JParticleTypeRegistry.AURA_BLOB);

    private final DefaultParticleType particleType;

    JParticleType(DefaultParticleType particleType) {
        this.particleType = particleType;
    }
}
