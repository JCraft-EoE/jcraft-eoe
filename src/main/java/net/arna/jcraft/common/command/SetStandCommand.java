package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.argumenttype.StandArgumentType;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

import static net.arna.jcraft.JCraft.summon;

public class SetStandCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .then(CommandManager.argument("stand", StandArgumentType.stand())
                                        .executes(SetStandCommand::run)
                                )
                        )
                )
        );
    }

    private static int run(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(ctx, "targets");
        StandType type = ctx.getArgument("stand", StandType.class);

        if (targets.isEmpty()) return 0;
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                IEntityDataSaver entityData = (IEntityDataSaver) livingEntity;
                entityData.getPersistentData().putInt("StandID", type.getId());

                livingEntity.detach();

                StandEntity stand = summon(entity.getWorld(), livingEntity);
                if (stand != null) stand.startRiding(livingEntity);
            }
        }

        return 1;
    }
}
