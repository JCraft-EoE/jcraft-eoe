package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.argumenttype.AttackArgumentType;
import net.arna.jcraft.common.command.permissions.JPerm;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.argumenttype.AttackArgumentType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public class InduceAttackCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("attack")
                .requires(JPerms.any(JPerms.ATTACK_STAND, JPerms.ATTACK_SPEC))
                .then(Commands.argument("ents", EntityArgument.entities())
                        .then(registerAttackType("stand", true, JPerms.ATTACK_STAND))
                        .then(registerAttackType("spec", false, JPerms.ATTACK_SPEC))
                )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerAttackType(String name, boolean stand, JPerm perm) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(name);

        root.requires(perm.require())
            .then(Commands.argument("attack", AttackArgumentType.attack())
                .executes(ctx -> runAttack(
                        ctx.getSource(),
                        EntityArgument.getEntities(ctx, "ents"),
                        stand,
                        false, // aerial
                        false, // crouching
                        ctx.getArgument("attack", MoveClass.class)
                )));

        root.then(registerState("ground", stand, false, false));
        root.then(registerState("air", stand, true, false));
        root.then(registerState("crouch", stand, false, true));
        root.then(registerState("air_crouch", stand, true, true));

        return root;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerState(
            String name,
            boolean stand,
            boolean aerial,
            boolean crouching
    ) {
        return Commands.literal(name)
                .then(Commands.argument("attack", AttackArgumentType.attack())
                        .executes(ctx -> runAttack(
                                ctx.getSource(),
                                EntityArgument.getEntities(ctx, "ents"),
                                stand,
                                aerial,
                                crouching,
                                ctx.getArgument("attack", MoveClass.class)
                        )));
    }

    public static int runAttack(
            final CommandSourceStack source,
            final Collection<? extends Entity> targets,
            final boolean stand,
            final boolean aerial,
            final boolean crouching,
            final MoveClass moveClass
    ) {
        int flag = 0;
        String typeName = moveClass.toString();

        if (stand) {
            for (Entity entity : targets) {
                if (entity instanceof LivingEntity living) {
                    JComponentPlatformUtils.getCooldowns(living).clear();
                    StandEntity<?, ?> standEntity = JComponentPlatformUtils.getStandComponent(living).getStand();

                    if (standEntity != null) {
                        living.setOnGround(!aerial);
                        living.setShiftKeyDown(crouching);

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
                        living.setOnGround(!aerial);
                        living.setShiftKeyDown(crouching);

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
