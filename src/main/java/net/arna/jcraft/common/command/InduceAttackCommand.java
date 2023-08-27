package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.common.argumenttype.AttackArgumentType;
import net.arna.jcraft.common.attack.core.old.MoveQueue;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

public class InduceAttackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("attack")
                .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                .then(CommandManager.argument("ents", EntityArgumentType.entities())
                        .then(CommandManager.literal("stand")
                                .then(CommandManager.argument("attack", AttackArgumentType.attack()).executes(
                                        context -> runAttack(
                                                EntityArgumentType.getEntities(context, "ents"),
                                                true,
                                                context.getArgument("attack", MoveQueue.class)
                                        )
                                ))
                        )
                        .then(CommandManager.literal("spec")
                                .then(CommandManager.argument("attack", AttackArgumentType.attack()).executes(
                                        context -> runAttack(
                                                EntityArgumentType.getEntities(context, "ents"),
                                                false,
                                                context.getArgument("attack", MoveQueue.class)
                                        )
                                ))
                        )
                )
        );
    }

    public static int runAttack(Collection<? extends Entity> targets, boolean stand, MoveQueue queue) {
        int flag = 0;

        if (stand) {
            for (Entity entity : targets) {
                CooldownsComponent cooldowns = JComponents.COOLDOWNS.get(entity);
                StandComponent standData = JComponents.STAND.get(entity);

                cooldowns.clear();

                StandEntity<?, ?> standEntity = standData.getStand();
                if (standEntity != null) {
                    standEntity.handleMove(queue.getMoveType());
                    flag = 1;
                }
            }
        } else {
            for (Entity entity : targets) {
                if (!(entity instanceof PlayerEntity player)) continue;

                JComponents.getCooldowns(player).clear();

                JSpec<?, ?> spec = JUtils.getSpec(player);
                if (spec != null) {
                    spec.initMove(queue.getMoveType());
                    flag = 1;
                }
            }
        }

        return flag;
    }
}
