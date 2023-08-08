package net.arna.jcraft.registry;

import net.arna.jcraft.common.command.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public interface JCommandRegistry {
    static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            InduceAttackCommand.register(dispatcher);
            AboutStandCommand.register(dispatcher);
            AboutSpecCommand.register(dispatcher);
            SetStandCommand.register(dispatcher);
            ClearStandCommand.register(dispatcher);
            SetSpecCommand.register(dispatcher);
            MoveDataCommand.register(dispatcher);
            StandSkinCommand.register(dispatcher);
            StandBlockCommand.register(dispatcher);
            GravityCommand.register(dispatcher);
            JConfigCommand.register(dispatcher);
        });
    }
}
