package net.arna.jcraft.registry;

import net.arna.jcraft.common.command.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public interface JCommandRegister {
    static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(InduceAttackCommand::register);
        CommandRegistrationCallback.EVENT.register(AboutStandCommand::register);
        CommandRegistrationCallback.EVENT.register(SetStandCommand::register);
        CommandRegistrationCallback.EVENT.register(SetSpecCommand::register);
        CommandRegistrationCallback.EVENT.register(FrameDataCommand::register);
    }
}
