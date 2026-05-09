package net.arna.jcraft.api.misc;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import lombok.Data;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * System for setting destruction states of blocks.
 * Minecraft doesn't much care for this at all and handles
 * all of it through {@link net.minecraft.client.multiplayer.MultiPlayerGameMode},
 * {@link net.minecraft.server.level.ServerPlayerGameMode} and
 * {@link net.minecraft.client.renderer.LevelRenderer},
 * hence we need to make our own.
 * <p>
 * All it does is tell clients how much a block is broken.
 * These clients then use that to render destruction state and
 * start from this value when they start breaking the block
 * (and we then tell the server to accept this).
 */
public class JBlockBreaker {
    private static final Map<Level, Map<BlockPos, BreakState>> breakStates = new WeakHashMap<>();
    private static int tickCounter = 0;

    @ApiStatus.Internal
    public static void init() {
        TickEvent.SERVER_LEVEL_POST.register(JBlockBreaker::tick);
        TickEvent.SERVER_POST.register(s -> tickCounter++);
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> onBlockBreak(level, pos));
    }

    /**
     * Sets the break state of the given position.
     * @param level The level in which this breakage is.
     * @param pos The position of the block that's breaking
     * @param breakage How much the block is broken ([0, 1])
     */
    public static void setBreakState(Level level, @Nullable LivingEntity breaker, BlockPos pos, float breakage) {
        BreakState breakState = new BreakState(pos, breakage + getBreakage(level, pos));
        Map<BlockPos, BreakState> breakStates = JBlockBreaker.breakStates.computeIfAbsent(level, l -> new HashMap<>());

        if (breakState.getBreakage() >= 1) {
            breakBlockAndDrop(level, breaker, pos);
            breakStates.remove(pos);
            return;
        }

        breakStates.put(pos, breakState);
        sendBreakStates(level, List.of(breakState));
    }

    /**
     * Sets the break state of multiple blocks
     * @param level The level these blocks are in
     * @param map A map of block positions to their breakage ([0, 1])
     */
    public static void setBreakState(Level level, @Nullable LivingEntity breaker, Object2FloatMap<BlockPos> map) {
        Pair<List<BreakState>, List<BreakState>> newBreakStates = map.object2FloatEntrySet().stream()
                .filter(entry -> entry.getFloatValue() > 0)
                .map(e -> new BreakState(e.getKey(), e.getFloatValue() +
                        getBreakage(level, e.getKey())))
                // Divide up into a group of break states and a group of broken blocks.
                .reduce(Pair.of(new ArrayList<>(), new ArrayList<>()),
                        (p, b) -> {
                            if (b.getBreakage() >= 1f) p.getSecond().add(b);
                            else p.getFirst().add(b);
                            return p;
                        }, (p1, p2) ->
                        Pair.of(Stream.concat(p1.getFirst().stream(), p2.getFirst().stream()).toList(),
                                Stream.concat(p1.getSecond().stream(), p2.getSecond().stream()).toList()));

        List<BreakState> breaking = newBreakStates.getFirst();
        List<BreakState> broken = newBreakStates.getSecond();
        Map<BlockPos, BreakState> breakStates = JBlockBreaker.breakStates.computeIfAbsent(level, l -> new HashMap<>());

        // Send breaking blocks
        if (!breaking.isEmpty()) {
            breaking.forEach(s -> breakStates.put(s.getPos(), s));
            sendBreakStates(level, breaking);
        }

        // Break broken blocks
        for (BreakState breakState : broken) {
            breakBlockAndDrop(level, breaker, breakState.getPos());
            breakStates.remove(breakState.getPos()); // If there was one, it's gone now.
        }
    }

    /**
     * Breaks the block at the given position and drops its loot, including xp (if applicable)
     * @param level The level the block is in
     * @param breaker The entity that broke the block
     * @param pos The position of the broken block
     */
    public static void breakBlockAndDrop(Level level, @Nullable LivingEntity breaker, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        level.removeBlock(pos, false);
        Block.dropResources(state, level, pos, blockEntity, breaker, ItemStack.EMPTY);
    }

    /**
     * Gets the breakage for the block at the given position, if there is one.
     * If there isn't, returns 0.
     * @param level The level the block is in
     * @param pos The position the block is at
     * @return The breakage of the block
     */
    public static float getBreakage(Level level, BlockPos pos) {
        BreakState breakState = breakStates.getOrDefault(level, Collections.emptyMap()).get(pos);
        return breakState == null ? 0f : breakState.getBreakage();
    }

    private static void sendBreakStates(Level level, List<BreakState> breakStates) {
        if (!(level instanceof ServerLevel serverLevel) || breakStates.isEmpty()) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(breakStates.size());

        Vec3 pos = null;
        for (int i = 0; i < breakStates.size(); i++) {
            double mult = 1.0 / (i + 1);
            BreakState breakState = breakStates.get(i);

            // Calculate average position of all break states
            if (pos == null)
                pos = breakState.getPos().getCenter();
            else pos = pos.multiply(i, i, i).add(breakState.getPos().getCenter()).multiply(mult, mult, mult);

            buf.writeBlockPos(breakState.getPos());
            buf.writeFloat(breakState.getBreakage());
        }

        Collection<ServerPlayer> players = JUtils.around(serverLevel, pos, 512);
        NetworkManager.sendToPlayers(players, JPacketRegistry.S2C_BLOCK_BREAKAGE, buf);
    }

    private static EventResult onBlockBreak(Level level, BlockPos pos) {
        // Remove any breakage state for this block when it's broken.
        breakStates.getOrDefault(level, Collections.emptyMap()).remove(pos);
        return EventResult.pass();
    }

    /**
     * Ticks down the progress of each break state every second.
     * @param level The level to tick for
     */
    private static void tick(ServerLevel level) {
        Map<BlockPos, BreakState> breakStates = JBlockBreaker.breakStates.get(level);
        if (breakStates == null) return;

        List<BlockPos> toRemove = new ArrayList<>();
        for (BreakState breakState : breakStates.values()) {
            breakState.tick();
            if (breakState.getBreakage() <= 0f) {
                toRemove.add(breakState.getPos());
            }
        }

        toRemove.forEach(breakStates::remove);
    }

    @Data
    public static class BreakState {
        private final BlockPos pos;
        private int lastUpdate;
        private float breakage;

        public BreakState(BlockPos pos, float breakage) {
            this.pos = pos;
            this.lastUpdate = tickCounter;
            this.breakage = breakage;
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeFloat(breakage);
        }

        public void tick() {
            if (tickCounter - lastUpdate >= 20) {
                breakage -= 0.1f;
                lastUpdate = tickCounter;
            }
        }
    }
}
