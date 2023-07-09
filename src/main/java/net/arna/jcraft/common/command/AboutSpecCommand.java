package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class AboutSpecCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("spec")
                .then(CommandManager.literal("about").executes(AboutSpecCommand::run)));
    }

    private static final List<String> buttons = List.of(
            "Heavy",
            "Barrage",
            "Special 1",
            "Special 2",
            "Special 3",
            "Ultimate"
    );

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity playerEntity = context.getSource().getPlayer();
        if (playerEntity == null) {
            JCraft.LOGGER.error("Tried to run /spec about command on invalid player, source: " + context.getSource());
            return 0;
        }

        JCraftSpec spec = JUtils.getSpec(playerEntity);
        if (spec != null) {
            StringBuilder readout = new StringBuilder("Name: §e");

            // Name
            readout.append(spec.getName()).append("§r\n");

            // Description
            readout.append("§a").append(spec.getDescription()).append("§r\n\n");

            /*
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
             */

            // Attacks
            readout.append("§2ATTACKS:§r\n");
            int i = 0;
            for (Attack a : spec.getAttacks()) {
                readout.append("§2● ").append(buttons.get(i)).append("§r - §5").append(a.name).append("§r - ").append(a.description).append("\n");
                i++;
            }

            // Details
            readout.append(spec.getDetails());

            playerEntity.sendMessage(Text.of(readout.toString()));
        } else {
            playerEntity.sendMessage(Text.of("No spec found!"), false);
            return 0;
        }

        return 1;
    }
}
