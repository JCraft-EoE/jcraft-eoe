package net.arna.jcraft.common.compat;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.FTBChunksProperties;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import net.arna.jcraft.platform.JPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility class to check whether a player may interact with a block
 * according to FTB Chunks.
 */
public class FtbChunksCompat {
    private static Itf instance;

    public static Itf get() {
        if (instance == null) {
            boolean isLoaded = JPlatformUtils.isModLoaded("ftbchunks");
            instance = isLoaded ? new Impl() : new Dummy();
        }

        return instance;
    }

    public interface Itf {
        boolean mayEdit(ServerPlayer player, ServerLevel level, BlockPos pos);
    }

    /**
     * Dummy implementation used when FTB Chunks is not installed.
     */
    private static class Dummy implements Itf {
        @Override
        public boolean mayEdit(ServerPlayer player, ServerLevel level, BlockPos pos) {
            return true;
        }
    }

    /**
     * Actual implementation used when FTB Chunks is installed.
     */
    private static class Impl implements Itf {
        private static FTBChunksAPI.API api;

        @Override
        public boolean mayEdit(ServerPlayer player, ServerLevel level, BlockPos pos) {
            if (!api.isManagerLoaded()) return true;

            ClaimedChunkManager manager = api.getManager();
            if (manager.getBypassProtection(player.getUUID())) return true;

            ClaimedChunk chunk = manager.getChunk(new ChunkDimPos(level, pos));
            // Chunk must be unclaimed or player must be member of the team.
            return chunk == null
                    // We only want to edit, not interact, but the former only exists on forge,
                    // while the latter only exists on Fabric. So either is fine.
                    || chunk.getTeamData().canPlayerUse(player, FTBChunksProperties.BLOCK_EDIT_MODE)
                    || chunk.getTeamData().canPlayerUse(player, FTBChunksProperties.BLOCK_EDIT_AND_INTERACT_MODE);
        }

        private void ensureInstance() {
            if (api == null) api = FTBChunksAPI.api();
        }
    }
}
