package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.common.effects.*;
import net.minecraft.world.effect.MobEffect;

import static net.arna.jcraft.JCraft.EFFECTS;

public interface JStatusRegistry {

    RegistrySupplier<MobEffect> DAZED = EFFECTS.register("dazed_effect", DazedStatusEffect::new);
    RegistrySupplier<MobEffect> KNOCKDOWN = EFFECTS.register("knockdown", KnockdownStatusEffect::new);

    RegistrySupplier<MobEffect> WSPOISON = EFFECTS.register("ws_poison", WSPoisonEffect::new);
    RegistrySupplier<MobEffect> STANDLESS = EFFECTS.register("standless", StandlessEffect::new);
    RegistrySupplier<MobEffect> OUTOFBODY = EFFECTS.register("outofbody", OutOfBodyEffect::new);
    RegistrySupplier<MobEffect> WEIGHTLESS = EFFECTS.register("weightless", WeightlessStatusEffect::new);
    RegistrySupplier<MobEffect> BLEEDING = EFFECTS.register("jbleeding", BleedingEffect::new);
    RegistrySupplier<MobEffect> PHPOISON = EFFECTS.register("phpoison", PurpleInfectionEffect::new);
    RegistrySupplier<MobEffect> HYPOXIA = EFFECTS.register("hypoxia", HypoxiaEffect::new);
    RegistrySupplier<WaterWalkingEffect> WATER_WALKING = EFFECTS.register("water_walking", WaterWalkingEffect::new);

    static void init() {
        // intentionally left empty
    }
}
