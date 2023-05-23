package net.arna.jcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.StandEntity;
import net.arna.jcraft.util.IEntityDataSaver;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.arna.jcraft.JCraft.Summon;

public class SetStandCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("ents", EntityArgumentType.entities())
                        .then(CommandManager.argument("id", IntegerArgumentType.integer(-10))
                            .executes(
                                    context -> run(context.getSource(), IntegerArgumentType.getInteger(context, "id"), EntityArgumentType.getEntities(context, "ents"))
                            )
                        )
                    )
                )
        );
    }

    public static int run(ServerCommandSource source, int id, Collection<? extends Entity> targets) throws CommandSyntaxException {
        PlayerEntity player = source.getPlayer();

        if (-JCraft.EVOLUTION_COUNT > id || id > JCraft.STAND_COUNT) {
            source.getPlayer().sendMessage(Text.of("Stand ID outside range!"));
            return 0;
        }

        if (player.hasPermissionLevel(2) || "Arna57".equals(source.getName())) {
            for (Entity entity : targets) {
                if (entity instanceof LivingEntity livingEntity) {
                    IEntityDataSaver entityData = (IEntityDataSaver)livingEntity;
                    entityData.getPersistentData().putInt("StandID", id);

                    livingEntity.detach();

                    StandEntity stand = Summon(source.getWorld(), livingEntity);
                    if (stand != null) {
                        stand.startRiding(livingEntity);
                    }
                }
            }
            return 1;
        }

        return 0;
    }
}
