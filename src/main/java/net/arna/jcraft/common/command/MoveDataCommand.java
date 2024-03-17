package net.arna.jcraft.common.command;

import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class MoveDataCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("movedata")
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

    public static int run(ServerCommandSource source, boolean stand) {
        PlayerEntity player = source.getPlayer();
        if (player == null)
            return 0;

        AbstractMove<?, ? extends IAttacker<?, ?>> move;

        if (stand) {
            StandEntity<?, ?> standEntity = JUtils.getStand(player);
            if (standEntity == null)
                return 0;
            else {
                move = standEntity.curMove;
                if (move == null) move = standEntity.prevMove;
            }
        } else {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec == null) {
                return 0;
            } else {
                move = spec.curMove;
                if (move == null) move = spec.previousAttack;
            }
        }

        if (move == null) return 0;

        int moveStun = move.getDuration();
        int initTime = move.getWindup();

        if (initTime > 0) initTime -= 1;

        int startup = initTime;
        StringBuilder frames = new StringBuilder();
        int recovery = move.getDuration() - initTime - 1;

        String advOnHit = "No physical hit\n";
        String advOnBlock = "";

        String mainFDMessage =
                "======== Move stats for: §2" + move.getName().getString() + "§r ========\n" +
                        "Move distance: §6" + move.getMoveDistance() + "§r m\n";


        int armor = move.getArmor();
        if (armor > 0)
            mainFDMessage = mainFDMessage.concat("§rAttack has: §7" + (armor > 100 ? "Hyper Armor§r\n" : armor + " Armor Points§r\n"));

        if (move instanceof AbstractSimpleAttack<?, ?> attack) {
            if (attack.getBlockableType() == BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
                mainFDMessage = mainFDMessage.concat("§rEffects on hit are §5UNBLOCKABLE§r\n");

            // Multihit vars
            boolean start = true;
            boolean fRec = false;
            int j = 0; // inter-recovery measurement

            if (attack.isCharge() && !attack.isBarrage()) {
                frames = new StringBuilder("§4until hit§r");
                recovery = 10;
            }
            // I REALLY don't want to go through the mental gymnastics of figuring out the maths that would do this faster, so I'm just going to simulate
            else if (attack instanceof AbstractBarrageAttack<?, ?> barrage) {
                int interval = barrage.getInterval();
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
                if (fRec) recovery = 0;
            } else if (attack instanceof AbstractMultiHitAttack<?, ?> multiHitAttack) {
                IntSortedSet atks = multiHitAttack.getHitMoments();
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
            } else {
                frames.append("§41§r");
            }

            if (attack.getHitboxSize() > 0 || !attack.getExtraHitBoxes().isEmpty()) {
                advOnHit = "Advantage on hit: §c" + (attack.getStun() - recovery - 1) + "§r ticks of " + attack.getStunType() + " Stun\n";
                advOnBlock = (attack.getBlockableType() == BlockableType.NON_BLOCKABLE) ? "§5Unblockable§r\n" : "Advantage on block: §5" + (attack.getBlockStun() - recovery) + "§r ticks";

                mainFDMessage = mainFDMessage.concat(
                        "Damage: §6" + attack.getDamage() / 2f + "§r hearts\n" +
                                "Knockback: §6" + attack.getKnockback()) + "§r\n";
            }
        }

        mainFDMessage = mainFDMessage.concat(
                "Startup: §b" + startup + "§r ticks\n" +
                        "Active: " + frames + " ticks\n" +
                        "Recovery: §a" + recovery + "§r ticks\n"
        );
        mainFDMessage = mainFDMessage.concat(advOnHit);
        mainFDMessage = mainFDMessage.concat(advOnBlock);

        player.sendMessage(Text.of(mainFDMessage), false);
        return 1;
    }
}
