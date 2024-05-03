package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.common.argumenttype.AttackArgumentType;
import net.arna.jcraft.common.attack.core.MoveInputType;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

public class InduceAttackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("attack")
                .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                .then(CommandManager.argument("ents", EntityArgumentType.entities())
                        .then(CommandManager.literal("stand")
                                .then(CommandManager.argument("attack", AttackArgumentType.attack()).executes(
                                        context -> runAttack(
                                                context.getSource(),
                                                EntityArgumentType.getEntities(context, "ents"),
                                                true,
                                                context.getArgument("attack", MoveType.class)
                                        )
                                ))
                        )
                        .then(CommandManager.literal("spec")
                                .then(CommandManager.argument("attack", AttackArgumentType.attack()).executes(
                                        context -> runAttack(
                                                context.getSource(),
                                                EntityArgumentType.getEntities(context, "ents"),
                                                false,
                                                context.getArgument("attack", MoveType.class)
                                        )
                                ))
                        )
                )
        );
    }

    public static int runAttack(ServerCommandSource source, Collection<? extends Entity> targets, boolean stand, MoveType type) {
        int flag = 0;
        String typeName = type.toString();

        if (stand) {
            for (Entity entity : targets) {
                if (entity instanceof LivingEntity living) {
                    JComponents.getCooldowns(living).clear();
                    StandEntity<?, ?> standEntity = JComponents.getStandData(living).getStand();

                    if (standEntity != null) {
                            if (standEntity.initMove(type)) {
                                source.sendFeedback(Text.literal("Initiating stand attack " + typeName + " for " + living.getName().getString()), true);
                            } else {
                                source.sendFeedback(Text.literal("Queueing stand attack " + typeName + " for " + living.getName().getString()), true);
                                standEntity.queueMove(MoveInputType.fromMoveType(type));
                            }

                        flag = 1;
                    }
                }
            }
        } else {
            for (Entity entity : targets) {
                if (!(entity instanceof PlayerEntity player)) continue;
                JComponents.getCooldowns(player).clear();
                JSpec<?, ?> spec = JUtils.getSpec(player);

                if (spec != null) {
                    if (spec.initMove(type)) {
                        source.sendFeedback(Text.literal("Initiating spec attack " + typeName + " for " + entity.getName().getString()), true);
                    } else {
                        source.sendFeedback(Text.literal("Queueing spec attack " + typeName + " for " + entity.getName().getString()), true);
                        spec.queuedMove = MoveInputType.fromMoveType(type);
                    }

                    flag = 1;
                }
            }
        }

        return flag;
    }
}
