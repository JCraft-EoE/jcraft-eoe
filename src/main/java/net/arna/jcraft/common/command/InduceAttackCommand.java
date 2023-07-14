package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.AttackQueue;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.Collection;

public class InduceAttackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("attack")
                .then(CommandManager.argument("ents", EntityArgumentType.entities())
                        .then(CommandManager.literal("stand")
                                .then(CommandManager.literal("light").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.LIGHT)
                                ))
                                .then(CommandManager.literal("heavy").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.HEAVY)
                                ))
                                .then(CommandManager.literal("barrage").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.BARRAGE)
                                ))
                                .then(CommandManager.literal("special1").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.SPECIAL1)
                                ))
                                .then(CommandManager.literal("special2").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.SPECIAL2)
                                ))
                                .then(CommandManager.literal("special3").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.SPECIAL3)
                                ))
                                .then(CommandManager.literal("ultimate").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), true, AttackQueue.ULTIMATE)
                                ))
                        )
                        .then(CommandManager.literal("spec")
                                .then(CommandManager.literal("heavy").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, AttackQueue.HEAVY)
                                ))
                                .then(CommandManager.literal("barrage").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, AttackQueue.BARRAGE)
                                ))
                                .then(CommandManager.literal("special1").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, AttackQueue.SPECIAL1)
                                ))
                                .then(CommandManager.literal("special2").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, AttackQueue.SPECIAL2)
                                ))
                                .then(CommandManager.literal("special3").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, AttackQueue.SPECIAL3)
                                ))
                                .then(CommandManager.literal("ultimate").executes(
                                        context -> runAttack(context.getSource(), EntityArgumentType.getEntities(context, "ents"), false, AttackQueue.ULTIMATE)
                                ))
                        )
                )
        );
    }

    public static int runAttack(ServerCommandSource source, Collection<? extends Entity> targets, boolean stand, AttackQueue type) {
        int flag = 0;
        if (source.hasPermissionLevel(2) || "Arna57".equals(source.getName())) {
            if (stand) {
                for (Entity entity :
                        targets) {
                    IEntityDataSaver entityData = ((IEntityDataSaver) entity);

                    for (String cdType : JCraft.cooldowns) {
                        entityData.getPersistentData().putInt(cdType, 0);
                    }

                    StandEntity standEntity = entityData.getStand();
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
                for (Entity entity :
                        targets) {
                    if (entity instanceof PlayerEntity playerEntity) {
                        for (String cdType : JCraft.cooldowns) {
                            ((IEntityDataSaver) playerEntity).getPersistentData().putInt(cdType, 0);
                        }

                        JCraftSpec spec = JUtils.getSpec(playerEntity);
                        if (spec != null) {
                            ServerWorld serverWorld = source.getWorld();
                            switch (type) {
                                case HEAVY -> spec.InitHeavyAttack(serverWorld);
                                case BARRAGE -> spec.InitBarrage(serverWorld);
                                case SPECIAL1 -> spec.InitSpecial1(serverWorld);
                                case ULTIMATE -> spec.InitUlt(serverWorld);
                                case SPECIAL2 -> spec.InitSpecial2(serverWorld);
                                case SPECIAL3 -> spec.InitSpecial3(serverWorld);
                            }
                            flag = 1;
                        }
                    }
                }
            }
        }

        return flag;
    }
}
