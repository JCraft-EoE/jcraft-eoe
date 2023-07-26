package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

public class StandSkinCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("skin")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .then(CommandManager.argument("skin", IntegerArgumentType.integer(0, 3))
                                        .executes(ctx -> run(ctx, ctx.getArgument("skin", Integer.class)))
                                )
                        )
                )
        );
    }

    public static int run(CommandContext<ServerCommandSource> ctx, int skin) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(ctx, "targets");
        if (targets.isEmpty()) return 0;
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                IEntityDataSaver entityData = (IEntityDataSaver) livingEntity;
                StandEntity stand = entityData.getStand();

                if (stand == null) continue;

                StandType type = stand.getStandType();
                if (skin <= type.getSkinCount())
                    entityData.getPersistentData().putInt("StandSkin", skin);

                livingEntity.detach();
                summon(entity.getWorld(), livingEntity);
            }
        }
        return 1;
    }
}
