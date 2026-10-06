package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

import static net.arna.jcraft.JCraft.summon;

public class StandSkinCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stand")
                .then(Commands.literal("skin")
                        .requires(JPerms.STAND_SKIN.require())
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("skin", IntegerArgumentType.integer(0, 3))
                                        .executes(ctx -> run(ctx, ctx.getArgument("skin", Integer.class)))
                                )
                        )
                )
        );
    }

    public static int run(final CommandContext<CommandSourceStack> ctx, final int skin) throws CommandSyntaxException {
        final Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        if (targets.isEmpty()) {
            return 0;
        }
        JPerms.checkTargets(ctx.getSource(), targets, JPerms.STAND_SKIN_OTHERS);
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(livingEntity);
                StandEntity<?, ?> stand = standData.getStand();

                if (stand == null) {
                    continue;
                }

                if (skin < stand.getStandData().getInfo().getSkinCount()) {
                    standData.setSkin(skin);
                }

                livingEntity.unRide();
                summon(entity.level(), livingEntity);
            }
        }
        return 1;
    }
}
