package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public class ResetSpecCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spec")
                .then(Commands.literal("reset")
                        .requires(JPerms.SPEC_RESET.require())
                        .then(Commands.argument("entities", EntityArgument.entities())
                                    .executes(ResetSpecCommand::run)
                        )
                )
        );
    }

    public static int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final Collection<? extends Entity> targets = EntityArgument.getEntities(context, "entities");

        if (targets.isEmpty()) {
            return 0;
        }
        JPerms.checkTargets(context.getSource(), targets, JPerms.SPEC_RESET_OTHERS);

        try {
            for (final Entity entity : targets) {
                if (entity instanceof LivingEntity living) {
                    final JSpec<?,?> spec = JUtils.getSpec(living);
                    if (spec != null) {
                        spec.reset();
                    }
                }
            }
        } catch (final Exception ex) {
            JCraft.LOGGER.error("Failed to reset spec!", ex);
            return 0;
        }

        return 1;
    }

}
