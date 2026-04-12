package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.common.attack.actions.*;
import net.arna.jcraft.api.attack.core.MoveActionType;

public interface JMoveActionTypeRegistry {
    DeferredRegister<MoveActionType<?>> MOVE_ACTION_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.MOVE_ACTION_TYPE_REGISTRY_KEY);

    RegistrySupplier<PlaySoundAction.Type> PLAY_SOUND = register("play_sound", PlaySoundAction.Type.INSTANCE);
    RegistrySupplier<CancelSpecMoveAction.Type> CANCEL_SPEC_MOVE = register("cancel_spec_move", CancelSpecMoveAction.Type.INSTANCE);
    RegistrySupplier<MetallicaAddIronAction.Type> METALLICA_ADD_IRON = register("metallica_add_iron", MetallicaAddIronAction.Type.INSTANCE);
    RegistrySupplier<EffectAction.Type> EFFECT = register("effect", EffectAction.Type.INSTANCE);
    RegistrySupplier<LungeAction.Type> LUNGE = register("lunge", LungeAction.Type.INSTANCE);
    RegistrySupplier<CMoonInversionAction.Type> CMOON_INVERSION = register("cmoon_inversion", CMoonInversionAction.Type.INSTANCE);
    RegistrySupplier<UserAnimationAction.Type> USER_ANIMATION = register("user_animation", UserAnimationAction.Type.INSTANCE);
    RegistrySupplier<RunCommandAction.Type> RUN_COMMAND = register("run_command", RunCommandAction.Type.INSTANCE);
    RegistrySupplier<ScoreboardAction.Type> SCOREBOARD = register("scoreboard", ScoreboardAction.Type.INSTANCE);
    RegistrySupplier<LightOnFireAction.Type> LIGHT_ON_FIRE = register("light_on_fire", LightOnFireAction.Type.INSTANCE);
    RegistrySupplier<LaunchUpAction.Type> LAUNCH_UP = register("launch_up", LaunchUpAction.Type.INSTANCE);
    RegistrySupplier<NotifyHamonStompAction.Type> NOTIFY_HAMON_STOMP_ACTION = register("notify_hamon_stomp_action", NotifyHamonStompAction.Type.INSTANCE);

    private static <T extends MoveActionType<?>> RegistrySupplier<T> register(String name, T type) {
        return MOVE_ACTION_TYPE_REGISTRY.register(name, () -> type);
    }
}
