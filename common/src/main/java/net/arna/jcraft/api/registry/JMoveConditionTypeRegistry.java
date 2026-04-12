package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.common.attack.conditions.HamonChargeCondition;
import net.arna.jcraft.common.attack.conditions.HoldingAnubisCondition;
import net.arna.jcraft.common.attack.conditions.MetallicaIronCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;

public interface JMoveConditionTypeRegistry {
    DeferredRegister<MoveConditionType<?>> MOVE_CONDITION_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.MOVE_CONDITION_TYPE_REGISTRY_KEY);

    RegistrySupplier<MoveConditionType<?>> METALLICA_IRON = register("metallica_iron", MetallicaIronCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HOLDING_ANUBIS = register("holding_anubis", HoldingAnubisCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_CHARGE = register("hamon_charge", HamonChargeCondition.Type.INSTANCE);

    private static RegistrySupplier<MoveConditionType<?>> register(String name, MoveConditionType<?> type) {
        return MOVE_CONDITION_TYPE_REGISTRY.register(name, () -> type);
    }
}
