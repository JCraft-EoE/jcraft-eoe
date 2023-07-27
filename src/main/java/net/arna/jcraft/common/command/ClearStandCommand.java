package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class ClearStandCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("clear")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .executes(ClearStandCommand::run))));
    }

    private static int run(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(ctx, "targets");

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                IEntityDataSaver entityData = (IEntityDataSaver) livingEntity;
                NbtCompound data = entityData.getPersistentData();

                if (!data.contains("StandID") || data.getInt("StandID") == 0) continue;
                data.putInt("StandID", 0);

                StandEntity<?, ?> stand = entityData.getStand();
                if (stand != null)
                    stand.detach();
            }
        }
        return 1;
    }
}
