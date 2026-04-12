package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public class ResetSpecCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spec")
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("entities", EntityArgument.entities())
                                    .executes(ResetSpecCommand::run)
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
                if (entity instanceof LivingEntity living) {
                    final SpecType specType = JComponentPlatformUtils.getSpecData(living).getType();
                    if (specType == JSpecTypeRegistry.VAMPIRE.get()) {
                        JComponentPlatformUtils.getVampirism(living).resetBlood();
                    }
                    else if (specType == JSpecTypeRegistry.HAMON.get()) {
                        var hamon = JComponentPlatformUtils.getHamon(living);
                        hamon.resetHamonCharge();
                        hamon.resetLastZoomPunched();
                        hamon.resetLastSendoed();
                        hamon.resetLastSendoAired();
                        hamon.resetLastStomped();
                        if (living instanceof final ServerPlayer player) {
                            revoke(player, JCraft.id("hamon1"));
                            revoke(player, JCraft.id("hamon2"));
                            revoke(player, JCraft.id("hamon3"));
                            revoke(player, JCraft.id("hamon4"));
                            revoke(player, JCraft.id("hamon5"));
                            revoke(player, JCraft.id("hamon6"));
                        }
                    }
                    else if (specType == JSpecTypeRegistry.ANUBIS.get()) {
                        JComponentPlatformUtils.getMiscData(living).setAttackSpeedMult(1f);
                    }
                }
            }
        } catch (final Exception ex) {
            JCraft.LOGGER.error("Failed to reset spec!", ex);
            return 0;
        }

        return 1;
    }

    private static void revoke(final ServerPlayer player, final ResourceLocation advancementLoc) {
        if (player.getServer() == null) {
            return;
        }
        final Advancement advancement = player.getServer().getAdvancements().getAdvancement(advancementLoc);
        if (advancement == null) {
            return;
        }
        final AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.hasProgress()) {
            for (final String crit : progress.getCompletedCriteria()) {
                player.getAdvancements().revoke(advancement, crit);
            }
        }
    }
}
