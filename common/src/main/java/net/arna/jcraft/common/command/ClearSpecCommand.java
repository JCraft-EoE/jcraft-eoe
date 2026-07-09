package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public class ClearSpecCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spec")
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .executes(ClearSpecCommand::run)
                        )
                )
        );
    }

    public static int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            final Collection<? extends Entity> targets = EntityArgument.getEntities(context, "entities");
            if (targets.isEmpty()) {
                return 0;
            }

            for (final Entity entity : targets) {
                if (!(entity instanceof LivingEntity living)) {
                    continue;
                }
                // Reset spec-specific state that lives outside the spec component (e.g. hamon charge
                // drives the bar visibility independently of spec type).
                final var specType = JComponentPlatformUtils.getSpecData(living).getType();
                if (specType == JSpecTypeRegistry.HAMON.get()) {
                    JComponentPlatformUtils.getHamon(living).resetHamonCharge();
                }
                JComponentPlatformUtils.getSpecData(living).setType(null);
            }
        } catch (final Exception ex) {
            JCraft.LOGGER.error("Failed to clear spec!", ex);
            return 0;
        }
        return 1;
    }
}
