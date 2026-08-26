package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.argumenttype.AttackArgumentType;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Collection;

public class InduceAttackCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("attack")
                .requires(JPerms.any(JPerms.ATTACK_STAND, JPerms.ATTACK_SPEC))
                .then(Commands.argument("ents", EntityArgument.entities())
                        .then(Commands.literal("stand")
                                .requires(JPerms.ATTACK_STAND.require())
                                .then(Commands.argument("attack", AttackArgumentType.attack()).executes(
                                        context -> runAttack(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "ents"),
                                                true,
                                                context.getArgument("attack", MoveClass.class)
                                        )
                                ))
                        )
                        .then(Commands.literal("spec")
                                .requires(JPerms.ATTACK_SPEC.require())
                                .then(Commands.argument("attack", AttackArgumentType.attack()).executes(
                                        context -> runAttack(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "ents"),
                                                false,
                                                context.getArgument("attack", MoveClass.class)
                                        )
                                ))
                        )
                )
        );
    }

    public static int runAttack(final CommandSourceStack source, final Collection<? extends Entity> targets, final boolean stand,
                                final MoveClass moveClass) throws CommandSyntaxException {
        JPerms.checkTargets(source, targets, JPerms.ATTACK_OTHERS);

        int flag = 0;
        String typeName = moveClass.toString();

        if (stand) {
            for (Entity entity : targets) {
                if (entity instanceof LivingEntity living) {
                    JComponentPlatformUtils.getCooldowns(living).clear();
                    StandEntity<?, ?> standEntity = JComponentPlatformUtils.getStandComponent(living).getStand();

                    if (standEntity != null) {
                        if (standEntity.initMove(moveClass)) {
                            source.sendSuccess(() -> Component.literal("Initiating stand attack " + typeName + " for " + living.getName().getString()), true);
                        } else {
                            source.sendSuccess(() -> Component.literal("Queueing stand attack " + typeName + " for " + living.getName().getString()), true);
                            standEntity.queueMove(MoveInputType.fromMoveClass(moveClass));
                        }

                        flag = 1;
                    }
                }
            }
        } else {
            for (Entity entity : targets) {
                if (entity instanceof LivingEntity living) {
                    JComponentPlatformUtils.getCooldowns(living).clear();
                    JSpec<?, ?> spec = JUtils.getSpec(living);

                    if (spec != null) {
                        if (spec.initMove(moveClass)) {
                            source.sendSuccess(() -> Component.literal("Initiating spec attack " + typeName + " for " + entity.getName().getString()), true);
                        } else {
                            source.sendSuccess(() -> Component.literal("Queueing spec attack " + typeName + " for " + entity.getName().getString()), true);
                            spec.queuedMove = MoveInputType.fromMoveClass(moveClass);
                        }

                        flag = 1;
                    }
                }
            }
        }

        return flag;
    }
}
