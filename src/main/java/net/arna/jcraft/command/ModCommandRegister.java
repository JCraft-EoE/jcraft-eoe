package net.arna.jcraft.command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommandRegister {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(InduceAttackCommand::register);
        CommandRegistrationCallback.EVENT.register(AboutStandCommand::register);
        CommandRegistrationCallback.EVENT.register(SetStandCommand::register);
        CommandRegistrationCallback.EVENT.register(SetSpecCommand::register);
        CommandRegistrationCallback.EVENT.register(FrameDataCommand::register);
    }
}
