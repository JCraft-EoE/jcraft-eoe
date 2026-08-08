package net.arna.jcraft.common.tickable;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class FrameDataRequests {
    private record Tick(boolean active) {

    }

    public enum FrameDataType {
        STAND, SPEC
    }

    private static class FrameData { // Named after a VERY common term, even though minecraft is updated in 20 TICKS per second, as opposed to 60 FRAMES per second
        public final long initialTick;
        public Queue<Tick> ticks = new LinkedList<>();
        public int finalAdvantage;
        public AbstractMove<?, ? super IAttacker<?, ?>> lastMove = null;
        @Getter
        private final FrameDataType type;

        FrameData(long initialTick, FrameDataType type) {
            this.initialTick = initialTick;
            this.type = type;
        }
    }

    private static final TickableHashMap<ServerPlayer, FrameData> frameDataRequests = new TickableHashMap<>();

    public static void add(ServerPlayer serverPlayer, FrameDataType type) {
        frameDataRequests.add(serverPlayer, new FrameData(serverPlayer.level().getGameTime(), type));
    }

    @SuppressWarnings("unchecked")
    public static void tick() {
        frameDataRequests.tick(iter -> {
            final Map.Entry<ServerPlayer, FrameData> entry = iter.next();
            final ServerPlayer player = entry.getKey();
            final FrameData frameData = entry.getValue();

            if (!player.isAlive()) {
                iter.remove();
                player.displayClientMessage(Component.translatable("jcraft.framedata.death"), false);
                return;
            }

            boolean wasActive = false;
            final IAttacker<?, ?> attacker;
            final AbstractMove<?, ? super IAttacker<?, ?>> move;
            if (frameData.getType() == FrameDataType.STAND) {
                StandEntity<?, ?> stand = JComponentPlatformUtils.getStandComponent(player).getStand();
                attacker = stand;
                if (stand != null) {
                    move = (AbstractMove<?, ? super IAttacker<?, ?>>) stand.getCurrentMove();
                } else {
                    move = null;
                }
            } else {
                JSpec<?, ?> spec = JComponentPlatformUtils.getSpecData(player).getSpec();
                attacker = spec;
                if (spec != null && spec.moveStun > 0) { // Spec moves persist after use
                    move = (AbstractMove<?, ? super IAttacker<?, ?>>) spec.getCurrentMove();
                } else {
                    move = null;
                }
            }

            // todo: find out why Anubis + Silver Chariot's God of Death only registers every second move here
            if (attacker == null) {
                if (frameData.lastMove != null) {
                    sendFrameData(player, frameData.lastMove, frameData.ticks);
                    iter.remove();
                }
                return;
            }
            if (move != null) wasActive = move.shouldPerform(attacker, attacker.getMoveStun());
            if (frameData.lastMove != null || move != null) frameData.ticks.add(new Tick(wasActive));

            final int moveStun = attacker.getMoveStun();
            if (moveStun > 0) { // If acting
                if (frameData.lastMove != move) {
                    if (move != null) {
                        player.displayClientMessage(
                                Component.literal("New move in sequence: " + move.getName().getString()), false
                        );
                    }
                } else if (move == null) {
                    if (frameData.lastMove != null) {
                        // Move finished but moveStun lingers, keep accumulating inactive ticks
                        frameData.ticks.add(new Tick(false));
                    }
                    //iter.remove();
                }
            } else if (frameData.lastMove != null) {
                sendFrameData(player, frameData.lastMove, frameData.ticks);
                iter.remove();
            }

            frameData.lastMove = move;
        });
    }

    private static void sendFrameData(@NonNull final ServerPlayer recipient, final AbstractMove<?, ?> move, @NonNull  final Queue<Tick> ticks) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        boolean isStartup = true;
        boolean wasActive = false;
        int startup = 0; // First part of a move, before any "active" ticks
        int recovery = 0; // Last part of a move, after any "active" ticks
        final Deque<Integer> actives = new LinkedList<>();

        while (!ticks.isEmpty()) {
            Tick tick = ticks.poll();
            boolean active = tick.active();

            if (wasActive != active) {
                actives.add(1);
            } else if (!actives.isEmpty()) {
                Integer activeCount = actives.pollLast();
                actives.add(activeCount + 1);
            }

            if (active) {
                isStartup = false;
                recovery = 0;
            } else {
                recovery++;
                if (first) {
                    builder.append("§b");
                }
            }

            if (isStartup) {
                startup++;
                recovery = 0;
            }

            wasActive = active;
            builder.append(active ? "§c●§r" : "●");
            first = false;
        }

        actives.pollLast(); // Last actives are just recovery

        final String frameDataString = builder.toString();
        int recoveryFromEnd = frameDataString.length() - recovery;
        final String frameDataDots = frameDataString.substring(0, recoveryFromEnd) + "§a" + frameDataString.substring(recoveryFromEnd);
        recipient.displayClientMessage(
                Component.literal(frameDataDots)
                , false
        );

        final StringBuilder activeFrameString = new StringBuilder();
        boolean isAttack = true; // As opposed to inter-attack, which is every second one
        while (!actives.isEmpty()) {
            Integer activeCount = actives.poll();
            activeFrameString.append(isAttack ? ("§c" + activeCount + "§r") : ("§8" + activeCount + "§r"));
            isAttack = !isAttack;
        }

        final String startupName = recovery > 0 ? "Startup" : "Duration";
        MutableComponent frameDataStats = Component.literal(startupName + ": §b" + startup + "§r ticks\n").setStyle(STARTUP_STYLE);
        if (!activeFrameString.isEmpty()) {
            frameDataStats.append(
                    Component.literal("Active: " + activeFrameString + " ticks\n").setStyle(ACTIVE_STYLE)
            );
        }
        if (recovery > 0) {
            frameDataStats.append(
                    Component.literal("Recovery: §a" + recovery + "§r ticks\n").setStyle(RECOVERY_STYLE)
            );
        }
        if (move != null) sendMoveInfo(recipient, move, recovery);
        recipient.displayClientMessage(frameDataStats, false);
    }

    private static final Component CLICK_FOR_DEFINITION = Component.translatable("jcraft.framedata.definition");
    private static final Style STARTUP_STYLE = Style.EMPTY
            .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_FOR_DEFINITION)
            ).withClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://glossary.infil.net/?t=Startup")
            );
    private static final Style ACTIVE_STYLE = Style.EMPTY
            .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_FOR_DEFINITION)
            ).withClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://glossary.infil.net/?t=Active")
            );
    private static final Style RECOVERY_STYLE = Style.EMPTY
            .withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_FOR_DEFINITION)
            ).withClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://glossary.infil.net/?t=Recovery")
            );

    private static void sendMoveInfo(@NonNull final ServerPlayer player, @NonNull final AbstractMove<?, ?> move, final int recovery) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("======== Last move: §2").append(move.getName().getString()).append("§r ========\n");
        stringBuilder.append("Move distance: §6").append(move.getMoveDistance()).append("§r m\n");

        final int armor = move.getArmor();
        if (armor > 0) {
            stringBuilder.append("§rAttack has: §7").append(armor == Integer.MAX_VALUE ? "Hyper Armor§r\n" : armor + " Armor Points§r\n");
        }

        if (move instanceof final AbstractSimpleAttack<?, ?> attack) {
            if (attack.getBlockableType() == BlockableType.NON_BLOCKABLE_EFFECTS_ONLY) {
                stringBuilder.append("§rEffects on hit are §5Unblockable§r\n");
            }

            if (attack.getHitboxSize() > 0 || !attack.getExtraHitBoxes().isEmpty()) {
                stringBuilder
                        .append("Damage: §6").append(attack.getDamage() / 2f).append("§r hearts\n")
                        .append("Knockback: §6").append(attack.getKnockback()).append("§r\n");

                stringBuilder.append("Advantage on hit: §c").append(attack.getStun() - recovery - 1).append("§r ticks of ").append(attack.getStunType()).append(" Stun\n");
                if (attack.getBlockableType() == BlockableType.NON_BLOCKABLE) {
                    stringBuilder.append("§5Unblockable§r\n");
                } else {
                    stringBuilder.append("Advantage on block: §5").append(attack.getBlockStun() - recovery).append("§r ticks");
                }

            } else {
                stringBuilder.append("No physical hit\n");
            }
        }
        player.displayClientMessage(Component.nullToEmpty(stringBuilder.toString()), false);
    }
}
