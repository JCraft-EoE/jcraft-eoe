package net.arna.jcraft.client.util;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.BlockDestructionProgress;

import java.util.SortedSet;

@Environment(EnvType.CLIENT)
public class BlockBreakerClient {
    // Storing these the exact same way as the LevelRenderer does to ensure we don't need to convert anything
    // in LevelRendererMixin upon every frame.
    private static final Long2ObjectMap<SortedSet<BlockDestructionProgress>> breakStates = new Long2ObjectOpenHashMap<>();

    public static void onBreakagePacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            BlockPos pos = buf.readBlockPos();
            int progress = buf.readVarInt();

            BlockDestructionProgress breakState = new BlockDestructionProgress(progress, pos);
            breakState.setProgress(progress % 10);

            breakStates.put(pos.asLong(), Util.make(Sets.newTreeSet(), s -> s.add(breakState)));
        }
    }

    public static ObjectSet<Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>>> getBreakStates() {
        return breakStates.long2ObjectEntrySet();
    }
}
