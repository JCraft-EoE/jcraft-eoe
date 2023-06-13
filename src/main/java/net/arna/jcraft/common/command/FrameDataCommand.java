package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.Attack;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class FrameDataCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("framedata")
                .then(CommandManager.literal("stand")
                        .executes(
                                context -> run(context.getSource(), true)
                        )
                )
                .then(CommandManager.literal("spec")
                        .executes(
                                context -> run(context.getSource(), false)
                        )
                )
        );
    }

    public static int run(ServerCommandSource source, boolean stand) throws CommandSyntaxException {
        PlayerEntity player = source.getPlayer();

        Attack attack;

        if (stand) {
            if (player.getFirstPassenger() instanceof StandEntity standEntity) {
                attack = standEntity.curAttack;
                if (attack == null) {
                    attack = standEntity.previousAttack;
                }
            } else {
                return 0;
            }
        } else {
            JCraftSpec spec = JCraftUtils.getSpec(player);
            if (spec != null) {
                attack = spec.curAttack;
                if (attack == null) {
                    attack = spec.previousAttack;
                }
            } else {
                return 0;
            }
        }

        if (attack == null) {
            return 0;
        }

        int moveStun = attack.moveStun;
        int initTime = attack.realInitTime();
        int stun = (int) (attack.stun * 20f);
        if (initTime > 0) {
            initTime -= 1;
        }

        StringBuilder frames = new StringBuilder();

        int startup = initTime;
        int recovery = 0;

        // Multihit vars
        boolean start = true;
        boolean fRec = false;
        int j = 0; // inter-recovery measurement

        switch (attack.attackType) {
            case CHARGE -> {
                frames = new StringBuilder("§4until hit§r");
                recovery = 10;
            }
            // I REALLY don't want to go through the mental gymnastics of figuring out the maths that would do this faster, so I'm just going to simulate
            case CHARGEBARRAGE, BARRAGE -> {
                int interval = attack.interval;
                for (int i = moveStun - 1; i > -1; i--) {
                    //JCraft.LOGGER.info(i + " " + (i % interval == 0) + " " + interval);
                    if (i % interval == 0) {
                        if (j > 0) {
                            if (start) {
                                startup = j;
                                start = false;
                            } else if (fRec) {
                                recovery = j;
                            } else {
                                frames.append(j).append(") ");
                            }
                            j = 0;
                        }
                        if (i + 1 > interval) {
                            frames.append("§41§r (");
                        } else {
                            frames.append("§41§r");
                            fRec = true;
                        }
                    } else {
                        j += 1;
                    }
                }
            }
            //if (j > 0 && !fRec) { frames.append(j); }
            case MULTIHIT -> {
                List<Integer> atks = attack.attackTimes;
                int c = 0;
                for (int i = moveStun - 1; i > -1; i--) {
                    //JCraft.LOGGER.info(i + " " + (moveStun - i) + " " + atks);
                    if (atks.contains(moveStun - i)) {
                        if (j > 0) {
                            if (start) {
                                startup = j;
                                start = false;
                            } else {
                                frames.append(j).append(") ");
                            }
                            j = 0;
                        }
                        c += 1;

                        if (c < atks.size()) {
                            frames.append("§41§r (");
                        } else {
                            frames.append("§41§r");
                            recovery = i;
                            break;
                        }
                    } else {
                        j += 1;
                    }
                }
            }
            default -> {
                frames = new StringBuilder("§41§r");
                recovery = attack.moveStun - initTime - 1;
            }
        }

        boolean effectOnlyUB = attack.ubEffectsOnly;
        String advOnBlock = (attack.unblockable && !effectOnlyUB) ? "§5UNBLOCKABLE" : "Advantage on block: §5" + (attack.getEffectiveBlockstun() - recovery) + "§r ticks";
        String mainFDMessage =
                "======== ATTACK STATS ========\n" +
                        "Startup: §b" + startup + "§r ticks\n" +
                        "Active: " + frames + " ticks\n" +
                        "Recovery: §a" + recovery + "§r ticks\n" +
                        "Advantage on hit: §c" + (stun - recovery - 1) + "§r ticks\n" +
                        advOnBlock;
        if (effectOnlyUB)
            mainFDMessage = mainFDMessage.concat("\n§5Effects on hit are UNBLOCKABLE");

        player.sendMessage(Text.of(mainFDMessage), false);
        return 1;
    }


}