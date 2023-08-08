package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class StandBlockCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("block")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .then(CommandManager.argument("block", BoolArgumentType.bool())
                                        .executes(ctx -> run(ctx, ctx.getArgument("block", Boolean.class)))
                                )
                        )
                )
        );
    }

    public static int run(CommandContext<ServerCommandSource> ctx, boolean block) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(ctx, "targets");
        if (targets.isEmpty()) return 0;
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                IEntityDataSaver entityData = (IEntityDataSaver) livingEntity;
                StandEntity<?, ?> stand = entityData.getStand();

                if (stand == null) continue;
                stand.blocking = block;
            }
        }
        return 1;
    }
}
