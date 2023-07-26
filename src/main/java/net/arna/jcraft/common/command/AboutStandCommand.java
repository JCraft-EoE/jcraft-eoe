package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.attack.Attack;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class AboutStandCommand {
    private static final List<String> buttons = List.of(
            "Light",
            "Heavy",
            "Barrage",
            "Special 1",
            "Ultimate",
            "Special 2",
            "Special 3",
            "Utility"
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("about").executes(AboutStandCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        PlayerEntity playerEntity = context.getSource().getPlayer();
        if (playerEntity == null) {
            JCraft.LOGGER.error("Tried to run /stand about command on invalid player, source: " + context.getSource());
            return 0;
        }

        if (playerEntity.getFirstPassenger() instanceof StandEntity stand) {
            StringBuilder readout = new StringBuilder("Name: §e");

            // Name
            readout.append(stand.getName().getString()).append("§r\n");

            // Description
            readout.append("§a").append(stand.description).append("§r\n\n");

            // Pros & Cons
            readout.append("§3PROS:§r\n");
            for (String s : stand.pros) {
                readout.append("§3●§r ").append(s).append("\n");
            }
            readout.append("§4CONS:§r\n");
            for (String s : stand.cons) {
                readout.append("§4●§r ").append(s).append("\n");
            }

            readout.append("\n");

            // Attacks
            readout.append("§2ATTACKS:§r\n");
            int i = 0;
            for (Attack a : stand.moves) {
                readout.append("§2● ").append(buttons.get(i)).append("§r - §5").append(a.name).append("§r - ").append(a.description).append("\n");

                Attack cV = a.getCrouchingVariation();
                if (cV != null)
                    readout.append("§3  ● CROUCHING ").append(buttons.get(i)).append("§r - §5").append(cV.name).append("§r - ").append(cV.description).append("\n");

                Attack aV = a.getAerialVariation();
                if (aV != null)
                    readout.append("§6  ● AERIAL ").append(buttons.get(i)).append("§r - §5").append(aV.name).append("§r - ").append(aV.description).append("\n");
                i++;
            }

            // Free Space
            readout.append(stand.freespace);

            playerEntity.sendMessage(Text.of(readout.toString()));
        } else {
            playerEntity.sendMessage(Text.translatable("jcraft.commands.error.nostand"), false);
            return 0;
        }

        return 1;
    }
}
