package net.arna.jcraft.api.misc;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<Level, Map<BlockPos, BlockDestructionProgress>> breakStates = new HashMap<>();

    /**
     * Sets the break state of the given position.
     * @param level The level in which this breakage is.
     * @param pos The position of the block that's breaking
     * @param breakage How much the block is broken ([0, 10])
     */
    public static void setBreakState(Level level, BlockPos pos, int breakage) {
        BlockDestructionProgress breakState = new BlockDestructionProgress(0, pos);
        breakState.setProgress(breakage % 10);
        breakStates.computeIfAbsent(level, l -> new HashMap<>()).put(pos, breakState);
        sendBreakStates(level, List.of(breakState));
    }

    /**
     * Sets the break state of multiple blocks
     * @param level The level these blocks are in
     * @param map A map of block positions to their breakage ([0, 1])
     */
    public static void setBreakState(Level level, Object2IntMap<BlockPos> map) {
        List<BlockDestructionProgress> newBreakStates = map.object2IntEntrySet().stream()
                .map(e -> Util.make(new BlockDestructionProgress(0, e.getKey()),
                        p -> p.setProgress(e.getIntValue())))
                .toList();
        newBreakStates.forEach(s -> breakStates.computeIfAbsent(level, l -> new HashMap<>())
                .put(s.getPos(), s));
        sendBreakStates(level, newBreakStates);
    }

    private static void sendBreakStates(Level level, List<BlockDestructionProgress> breakStates) {
        if (!(level instanceof ServerLevel serverLevel) || breakStates.isEmpty()) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(breakStates.size());

        Vec3 pos = null;
        for (int i = 0; i < breakStates.size(); i++) {
            double mult = 1.0 / (i + 1);
            BlockDestructionProgress breakState = breakStates.get(i);

            // Calculate average position of all break states
            if (pos == null)
                pos = breakState.getPos().getCenter();
            else pos = pos.multiply(i, i, i).add(breakState.getPos().getCenter()).multiply(mult, mult, mult);

            buf.writeBlockPos(breakState.getPos());
            buf.writeVarInt(breakState.getProgress());
        }

        Collection<ServerPlayer> players = JUtils.around(serverLevel, pos, 512);
        NetworkManager.sendToPlayers(players, JPacketRegistry.S2C_BLOCK_BREAKAGE, buf);
    }
}
