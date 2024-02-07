package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.component.JComponents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class ClearStandCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("clear")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .executes(ClearStandCommand::run))));
    }

    @SuppressWarnings("SameReturnValue")
    private static int run(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(ctx, "targets");

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                StandComponent standData = JComponents.STAND.get(livingEntity);

                if (standData.getType() == null) continue;
                standData.setType(null);

                StandEntity<?, ?> stand = standData.getStand();
                if (stand != null)
                    stand.detach();
            }
        }
        return 1;
    }
}
