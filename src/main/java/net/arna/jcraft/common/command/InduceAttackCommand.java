package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.common.attack.core.old.MoveQueue;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.Collection;

public class InduceAttackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("attack")
                .requires(source -> source.hasPermissionLevel(2) || "Arna57".equals(source.getName()) || "MrSterner".equals(source.getName()))
                .then(CommandManager.argument("ents", EntityArgumentType.entities())
                        .then(CommandManager.literal("stand")
                                .then(CommandManager.literal("light").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.LIGHT)
                                ))
                                .then(CommandManager.literal("heavy").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.HEAVY)
                                ))
                                .then(CommandManager.literal("barrage").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.BARRAGE)
                                ))
                                .then(CommandManager.literal("special1").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.SPECIAL1)
                                ))
                                .then(CommandManager.literal("special2").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.SPECIAL2)
                                ))
                                .then(CommandManager.literal("special3").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.SPECIAL3)
                                ))
                                .then(CommandManager.literal("ultimate").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, MoveQueue.ULTIMATE)
                                ))
                        )
                        .then(CommandManager.literal("spec")
                                .then(CommandManager.literal("heavy").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, MoveQueue.HEAVY)
                                ))
                                .then(CommandManager.literal("barrage").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, MoveQueue.BARRAGE)
                                ))
                                .then(CommandManager.literal("special1").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, MoveQueue.SPECIAL1)
                                ))
                                .then(CommandManager.literal("special2").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, MoveQueue.SPECIAL2)
                                ))
                                .then(CommandManager.literal("special3").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, MoveQueue.SPECIAL3)
                                ))
                                .then(CommandManager.literal("ultimate").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, MoveQueue.ULTIMATE)
                                ))
                        )
                )
        );
    }

    public static int runAttack(ServerCommandSource source, Collection<? extends Entity> targets, boolean stand, MoveQueue type) {
        int flag = 0;
        if (stand) {
            for (Entity entity : targets) {
                CooldownsComponent cooldowns = JComponents.COOLDOWNS.get(entity);
                StandComponent standData = JComponents.STAND.get(entity);

                cooldowns.clear();

                StandEntity<?, ?> standEntity = standData.getStand();
                if (standEntity != null) {
                    switch (type) {
                        case LIGHT -> standEntity.initLightAttack();
                        case HEAVY -> standEntity.initHeavyAttack();
                        case BARRAGE -> standEntity.initBarrage();
                        case SPECIAL1 -> standEntity.initSpecial1();
                        case SPECIAL2 -> standEntity.initSpecial2();
                        case SPECIAL3 -> standEntity.initSpecial3();
                        case ULTIMATE -> standEntity.initUlt(); // What the fuck????????
                    }
                    flag = 1;
                }
            }
        } else {
            for (Entity entity : targets) {
                if (!(entity instanceof PlayerEntity player)) continue;
                JComponents.getCooldowns(player).clear();

                JSpec spec = JUtils.getSpec(player);
                if (spec != null) {
                    ServerWorld serverWorld = source.getWorld();
                    switch (type) {
                        case HEAVY -> spec.initHeavyAttack(serverWorld);
                        case BARRAGE -> spec.initBarrage(serverWorld);
                        case SPECIAL1 -> spec.initSpecial1(serverWorld);
                        case ULTIMATE -> spec.initUlt(serverWorld);
                        case SPECIAL2 -> spec.initSpecial2(serverWorld);
                        case SPECIAL3 -> spec.initSpecial3(serverWorld);
                    }
                    flag = 1;
                }
            }
        }

        return flag;
    }
}
