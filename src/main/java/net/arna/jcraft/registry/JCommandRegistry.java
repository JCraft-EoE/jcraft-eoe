package net.arna.jcraft.registry;

import net.arna.jcraft.common.command.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public interface JCommandRegistry {
    static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            InduceAttackCommand.register(dispatcher, registryAccess, environment);
            AboutStandCommand.register(dispatcher);
            AboutSpecCommand.register(dispatcher, registryAccess, environment);
            SetStandCommand.register(dispatcher, registryAccess, environment);
            ClearStandCommand.register(dispatcher, registryAccess, environment);
            SetSpecCommand.register(dispatcher, registryAccess, environment);
            MoveDataCommand.register(dispatcher, registryAccess, environment);
            StandSkinCommand.register(dispatcher, registryAccess, environment);
            GravityCommand.register(dispatcher, registryAccess, environment);
            JConfigCommand.register(dispatcher);
        });
    }
}
