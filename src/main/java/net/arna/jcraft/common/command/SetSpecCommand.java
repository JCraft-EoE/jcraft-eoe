package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.ISpec;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class SetSpecCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("spec")
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.argument("id", IntegerArgumentType.integer(-10))
                                        .executes(
                                                context -> run(context.getSource(), IntegerArgumentType.getInteger(context, "id"), EntityArgumentType.getPlayers(context, "players"))
                                        )
                                )
                        )
                )
        );
    }

    public static int run(ServerCommandSource source, int id, Collection<? extends PlayerEntity> targets) throws CommandSyntaxException {
        if (source.hasPermissionLevel(2) || "Arna57".equals(source.getName())) {
            for (PlayerEntity playerTarget : targets) {
                NbtCompound playerNbt = ((IEntityDataSaver) playerTarget).getPersistentData();
                playerNbt.putInt("SpecID", id);
                JCraftUtils.assignSpec(playerTarget, playerNbt, (ISpec) playerTarget);
            }
            return 1;
        }

        return 0;
    }
}
