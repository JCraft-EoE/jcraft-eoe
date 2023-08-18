package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.attack.core.old.Attack;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class AboutSpecCommand {
    private static final List<String> buttons = List.of(
            "Heavy",
            "Barrage",
            "Special 1",
            "Special 2",
            "Special 3",
            "Ultimate"
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spec")
                .then(CommandManager.literal("about").executes(AboutSpecCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        JCraftSpec spec = JUtils.getSpec(player);
        if (spec != null) {
            StringBuilder readout = new StringBuilder("Name: §e");

            // Name
            readout.append(spec.getTranslatableName().getString()).append("§r\n");

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
                if (a.getCrouchingVariation() != null)
                    readout.append("§3  ●CROUCHING ").append(buttons.get(i)).append("§r - §5").append(a.name).append("§r - ").append(a.description).append("\n");
                if (a.getAerialVariation() != null)
                    readout.append("§4  ●AERIAL ").append(buttons.get(i)).append("§r - §5").append(a.name).append("§r - ").append(a.description).append("\n");
                i++;
            }

            // Details
            readout.append(spec.getDetails());

            player.sendMessage(Text.of(readout.toString()));
        } else {
            player.sendMessage(Text.translatable("jcraft.commands.error.nospec"), false);
            return 0;
        }

        return 1;
    }
}
