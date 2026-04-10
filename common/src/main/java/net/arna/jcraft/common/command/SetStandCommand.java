package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.argumenttype.StandArgumentType;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;

import static net.arna.jcraft.JCraft.summon;

public class SetStandCommand {
    private static final DynamicCommandExceptionType INVALID_SKIN = new DynamicCommandExceptionType(s ->
            Component.literal("The given stand only has " + s + " skins."));
    private static final SimpleCommandExceptionType EXCLUSIVE_STANDS_MULTIPLE_PLAYERS = new SimpleCommandExceptionType(
            Component.literal("Cannot give multiple players the same stand as 'Exclusive Stands' is enabled."));
    private static final SimpleCommandExceptionType EXCLUSIVE_STANDS_STAND_TAKEN = new SimpleCommandExceptionType(
            Component.literal("That stand is already used by another player."));

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        final RandomSource rng = RandomSource.create();
        dispatcher.register(Commands.literal("stand")
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("stand", StandArgumentType.stand())
                                        .executes(ctx -> executeSet(ctx, ctx.getArgument("stand", StandType.class), 0))
                                        .then(Commands.argument("skin", IntegerArgumentType.integer(0, 3))
                                                .executes(ctx -> executeSet(ctx,
                                                        ctx.getArgument("stand", StandType.class), ctx.getArgument("skin", Integer.class)))))
                                .then(Commands.literal("random")
                                        .executes(ctx -> executeSet(ctx, 0, rng)))
                        )));
    }

    private static int executeSet(final CommandContext<CommandSourceStack> ctx, final int skin, final RandomSource rng) throws CommandSyntaxException {
        return executeSet(ctx, null, skin, rng);
    }

    private static int executeSet(final CommandContext<CommandSourceStack> ctx, final StandType type, final int skin) throws CommandSyntaxException {
        return executeSet(ctx, type, skin, null);
    }

    private static int executeSet(final CommandContext<CommandSourceStack> ctx, final StandType type, final int skin, final RandomSource rng) throws CommandSyntaxException {
        final Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        if (targets.isEmpty() || (type == null && rng == null)) {
            return 0;
        }

        if (type != null && skin >= type.getData().getInfo().getSkinCount()) {
            throw INVALID_SKIN.create(type.getData().getInfo().getSkinCount());
        }

        // Check if the player is allowed to use the stand
        if (JServerConfig.EXCLUSIVE_STANDS.getValue()) {
            // Throw error if multiple players are selected and exclusive stands are enabled
            List<Player> playerTargets = targets.stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player) e)
                    .toList();
            int playerTargetCount = playerTargets.size();
            if (playerTargetCount > 0) {
                if (playerTargetCount > 1) {
                    throw EXCLUSIVE_STANDS_MULTIPLE_PLAYERS.create();
                }

                // Check if the stand is already taken by another player
                CommonStandComponent firstPlayerStandComp = JComponentPlatformUtils.getStandComponent(playerTargets.get(0));
                if (firstPlayerStandComp.getType() != type && JCraft.getExclusiveStandsData().isStandUsed(type)) {
                    throw EXCLUSIVE_STANDS_STAND_TAKEN.create();
                }
            }
        }

        for (final Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.getType().is(JTagRegistry.CAN_NEVER_HAVE_STAND)) {
                    continue;
                }
                final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(livingEntity);
                if (type != null) {
                    standData.setTypeAndSkin(type, skin);
                }
                else { // i.e. rng != null
                    standData.setType(StandTypeUtil.getRandom(rng));
                }

                livingEntity.unRide();
                summon(entity.level(), livingEntity);
            }
        }

        return 1;
    }
}
