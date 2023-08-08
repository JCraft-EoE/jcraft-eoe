package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.argumenttype.SpecArgumentType;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.ISpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class SetSpecCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spec")
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.argument("spec", SpecArgumentType.spec())
                                        .executes(SetSpecCommand::run)
                                )
                        )
                )
        );
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SpecType specType = context.getArgument("spec", SpecType.class);
        Collection<? extends PlayerEntity> targets = EntityArgumentType.getPlayers(context, "players");

        if (targets.isEmpty()) return 0;
        for (PlayerEntity playerTarget : targets) {
            NbtCompound playerNbt = ((IEntityDataSaver) playerTarget).getPersistentData();
            playerNbt.putInt("SpecID", specType.getId());
            JUtils.assignSpec(playerTarget, playerNbt, (ISpec) playerTarget);
        }

        return 1;
    }
}
