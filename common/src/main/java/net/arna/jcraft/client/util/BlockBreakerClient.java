package net.arna.jcraft.client.util;

import com.google.common.collect.Sets;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.arna.jcraft.common.events.JBlockEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

@Environment(EnvType.CLIENT)
public class BlockBreakerClient {
    // Storing these the exact same way as the LevelRenderer does to ensure we don't need to convert anything
    // in LevelRendererMixin upon every frame.
    private static final Long2ObjectMap<SortedSet<BlockDestructionProgress>> breakStates = new Long2ObjectOpenHashMap<>();
    private static final TreeSet<BlockDestructionProgress> emptySet = Sets.newTreeSet();
    private static int tickCounter = 0;
    private static final Object lock = new Object();

    public static void onBreakagePacket(FriendlyByteBuf buf) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = Objects.requireNonNull(minecraft.level);

        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            BlockPos pos = buf.readBlockPos();
            int progress = Mth.clamp((int) (buf.readFloat() * 10), 0, 10);
            int breakerId = buf.readVarInt();

            Entity breaker = breakerId > 0 ? level.getEntity(breakerId) : null;
            Player player = breaker instanceof Player p ? p : null;

            BlockDestructionProgress breakState = new BlockDestructionProgress(0, pos);
            breakState.updateTick(tickCounter);
            breakState.setProgress(progress);

            synchronized (lock) {
                TreeSet<BlockDestructionProgress> set = Sets.newTreeSet();
                set.add(breakState);
                SortedSet<BlockDestructionProgress> prev = breakStates.put(pos.asLong(), set);

                // New block attack, attack the block
                if (player != null && (prev == null || prev.isEmpty() ||
                        prev.stream().allMatch(p -> p.getProgress() <= 0))) {
                    minecraft.execute(() -> {
                        Objects.requireNonNull(level).getBlockState(pos).attack(level, pos, player);
                    });
                }
            }
        }
    }

    public static boolean isEmpty() {
        synchronized (lock) {
            return breakStates.isEmpty();
        }
    }

    public static ObjectSet<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> getBreakStates() {
        synchronized (lock) {
            return new ObjectOpenHashSet<>(breakStates.long2ObjectEntrySet());
        }
    }

    public static int getBreakProgress(BlockPos pos) {
        int progress = 0;

        synchronized (lock) {
            for (BlockDestructionProgress p : breakStates.getOrDefault(pos.asLong(), emptySet)) {
                if (p.getProgress() > progress) progress = p.getProgress();
            }
        }

        return progress;
    }

    public static void init() {
        ClientTickEvent.CLIENT_LEVEL_POST.register(BlockBreakerClient::tick);
        JBlockEvents.BEFORE_SET.register((pos, oldState, newState, level) -> onBlockBreak(pos));
    }

    private static EventResult onBlockBreak(BlockPos pos) {
        // Remove any breakage state for this block when it's broken.
        synchronized (lock) {
            breakStates.remove(pos.asLong());
        }
        return EventResult.pass();
    }

    private static void tick(ClientLevel level) {
        LongSet toRemove = new LongArraySet();
        synchronized (lock) {
            breakStates.long2ObjectEntrySet().forEach(e -> {
                e.getValue().forEach(BlockBreakerClient::tick);
                e.getValue().removeIf(p -> p.getProgress() <= 0);

                if (e.getValue().isEmpty()) toRemove.add(e.getLongKey());
            });

            toRemove.forEach(breakStates::remove);
        }

        tickCounter++;
    }

    private static void tick(BlockDestructionProgress breakState) {
        if (tickCounter - breakState.getUpdatedRenderTick() >= 20) {
            breakState.updateTick(tickCounter);
            breakState.setProgress(breakState.getProgress() - 1);
        }
    }
}
