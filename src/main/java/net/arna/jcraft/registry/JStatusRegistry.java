package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.effects.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface JStatusRegistry {
    StatusEffect DAZED = new DazedStatusEffect();
    StatusEffect KNOCKDOWN = new KnockdownStatusEffect();
    StatusEffect WSPOISON = new WSPoisonEffect();
    StatusEffect STANDLESS = new StandlessEffect();
    StatusEffect OUTOFBODY = new OutOfBodyEffect();
    StatusEffect WEIGHTLESS = new WeightlessStatusEffect();
    StatusEffect BLEEDING = new BleedingEffect();
    StatusEffect PHPOISON = new PurpleInfectionEffect();

    static void registerStatuses() {
        Registry.register(
                Registries.STATUS_EFFECT, JCraft.id("dazed_effect"),
                DAZED.addAttributeModifier(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        "FE04CA6A-A3D1-E22B-CB00-EDA6A853F90E",
                        -1.0,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                ).addAttributeModifier(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        "CB402E34-0AAC-383B-B26B-B253430DEEEA",
                        -1.0,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                ).addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        "778B48FC-485B-5BA7-58C7-E0D755CE354D",
                        0,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                ).addAttributeModifier(
                        EntityAttributes.GENERIC_FLYING_SPEED,
                        "516B532C-D1D9-C3A0-8970-A2C0A38CC452",
                        0,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );

        Registry.register(
                Registries.STATUS_EFFECT, JCraft.id("knockdown"),
                KNOCKDOWN.addAttributeModifier(
                        EntityAttributes.GENERIC_ARMOR,
                        "BB2CA307-EEA6-B54C-B324-F7EB036289BF",
                        30.0,
                        EntityAttributeModifier.Operation.ADDITION
                ).addAttributeModifier(
                        EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                        "3B9E3E15-2B1A-13F6-73D8-AB84287E7DF0",
                        20.0,
                        EntityAttributeModifier.Operation.ADDITION
                ).addAttributeModifier(
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        "ADA0E470-7D4D-DF4E-DDB2-A1858C84236C",
                        0.0,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
        );

        Registry.register(Registries.STATUS_EFFECT, JCraft.id("ws_poison"), WSPOISON);
        Registry.register(Registries.STATUS_EFFECT, JCraft.id("standless"), STANDLESS);
        Registry.register(Registries.STATUS_EFFECT, JCraft.id("outofbody"), OUTOFBODY);
        Registry.register(Registries.STATUS_EFFECT, JCraft.id("weightless"), WEIGHTLESS);
        Registry.register(Registries.STATUS_EFFECT, JCraft.id("bleeding"), BLEEDING);
        Registry.register(Registries.STATUS_EFFECT, JCraft.id("phpoison"), PHPOISON);
    }
}
