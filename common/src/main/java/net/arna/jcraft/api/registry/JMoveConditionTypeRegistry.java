package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.common.attack.conditions.HamonBreathCondition;
import net.arna.jcraft.common.attack.conditions.HamonChargeCondition;
import net.arna.jcraft.common.attack.conditions.HamonOverdriveCondition;
import net.arna.jcraft.common.attack.conditions.HamonSendoWaveKickAerialCondition;
import net.arna.jcraft.common.attack.conditions.HamonSendoWaveKickGroundedCondition;
import net.arna.jcraft.common.attack.conditions.HamonWaveCondition;
import net.arna.jcraft.common.attack.conditions.HamonZoomPunchCondition;
import net.arna.jcraft.common.attack.conditions.HoldingAnubisCondition;
import net.arna.jcraft.common.attack.conditions.MetallicaIronCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;

public interface JMoveConditionTypeRegistry {
    DeferredRegister<MoveConditionType<?>> MOVE_CONDITION_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.MOVE_CONDITION_TYPE_REGISTRY_KEY);

    RegistrySupplier<MoveConditionType<?>> METALLICA_IRON = register("metallica_iron", MetallicaIronCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HOLDING_ANUBIS = register("holding_anubis", HoldingAnubisCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_CHARGE = register("hamon_charge", HamonChargeCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_BREATH = register("hamon_breath", HamonBreathCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_OVERDRIVE = register("hamon_overdrive", HamonOverdriveCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_SENDO_AERIAL = register("hamon_sendo_wave_kick_aerial", HamonSendoWaveKickAerialCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_SENDO_GROUNDED = register("hamon_sendo_wave_kick_grounded", HamonSendoWaveKickGroundedCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_WAVE = register("hamon_wave", HamonWaveCondition.Type.INSTANCE);
    RegistrySupplier<MoveConditionType<?>> HAMON_ZOOM = register("hamon_zoom_punch", HamonZoomPunchCondition.Type.INSTANCE);

    private static RegistrySupplier<MoveConditionType<?>> register(String name, MoveConditionType<?> type) {
        return MOVE_CONDITION_TYPE_REGISTRY.register(name, () -> type);
    }
}
