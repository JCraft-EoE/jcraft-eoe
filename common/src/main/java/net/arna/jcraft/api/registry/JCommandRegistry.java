package net.arna.jcraft.api.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.common.command.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface JCommandRegistry {
    static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        InduceAttackCommand.register(dispatcher);
        AboutStandCommand.register(dispatcher);
        AboutSpecCommand.register(dispatcher);
        SetStandCommand.register(dispatcher);
        ClearStandCommand.register(dispatcher);
        SetSpecCommand.register(dispatcher);
        ResetSpecCommand.register(dispatcher);
        UnlockSpecCommand.register(dispatcher);
        ClearSpecCommand.register(dispatcher);
        FrameDataCommand.register(dispatcher);
        StandSkinCommand.register(dispatcher);
        StandBlockCommand.register(dispatcher);
        GravityCommand.register(dispatcher);
        JConfigCommand.register(dispatcher);
        JCraftHelpCommand.register(dispatcher);
        JCraftChangesCommand.register(dispatcher);
        CooldownCancelCommand.register(dispatcher);
    }
}
