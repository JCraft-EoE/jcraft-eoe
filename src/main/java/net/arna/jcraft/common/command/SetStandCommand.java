package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.arna.jcraft.common.argumenttype.StandArgumentType;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.arna.jcraft.JCraft.summon;

public class SetStandCommand {
    private static final DynamicCommandExceptionType INVALID_SKIN = new DynamicCommandExceptionType(s ->
            Text.literal("The given stand only has " + s + " skins."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stand")
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .then(CommandManager.argument("stand", StandArgumentType.stand())
                                        .executes(ctx -> executeSet(ctx, 0))
                                        .then(CommandManager.argument("skin", IntegerArgumentType.integer(0, 3))
                                                .executes(ctx -> executeSet(ctx, ctx.getArgument("skin", Integer.class))))))));
    }

    private static int executeSet(CommandContext<ServerCommandSource> ctx, int skin) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(ctx, "targets");
        if (targets.isEmpty()) return 0;

        StandType type = ctx.getArgument("stand", StandType.class);
        if (skin > type.getSkinCount()) throw INVALID_SKIN.create(type.getSkinCount());

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                StandComponent standData = JComponents.getStandData(livingEntity);
                standData.setTypeAndSkin(type, skin);

                livingEntity.detach();
                summon(entity.getWorld(), livingEntity);
            }
        }

        return 1;
    }
}
