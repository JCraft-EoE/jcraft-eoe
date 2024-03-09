package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

import static net.arna.jcraft.JCraft.summon;

public class StandSkinCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("skin")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
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
                StandComponent standData = JComponents.getStandData(livingEntity);
                StandEntity<?, ?> stand = standData.getStand();

                if (stand == null) continue;

                StandType type = stand.getStandType();
                if (skin <= type.getSkinCount())
                    standData.setSkin(skin);

                livingEntity.detach();
                summon(entity.getWorld(), livingEntity);
            }
        }
        return 1;
    }
}
