package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.Collection;

public class ClearStandCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stand")
                .then(Commands.literal("clear")
                        .requires(JPerms.STAND_CLEAR.require())
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(ClearStandCommand::run))));
    }

    @SuppressWarnings("SameReturnValue")
    private static int run(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        JPerms.checkTargets(ctx.getSource(), targets, JPerms.STAND_CLEAR_OTHERS);

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(livingEntity);

                if (standData.getType() == null) {
                    continue;
                }
                standData.setType(null);

                StandEntity<?, ?> stand = standData.getStand();
                if (stand != null) {
                    stand.unRide();
                }
            }
        }
        return 1;
    }
}
