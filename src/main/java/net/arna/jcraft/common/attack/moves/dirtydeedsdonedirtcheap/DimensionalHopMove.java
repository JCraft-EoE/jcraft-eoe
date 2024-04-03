package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.tickable.PastDimensions;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.mixin.ChunkLightProviderAccessor;
import net.arna.jcraft.mixin.LightStorageAccessor;
import net.arna.jcraft.mixin.LightingProviderAccessor;
import net.arna.jcraft.registry.JDimensionRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DimensionalHopMove extends AbstractSimpleAttack<DimensionalHopMove, D4CEntity> {
    public DimensionalHopMove(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public void onInitiate(D4CEntity attacker) {
        super.onInitiate(attacker);

        JCraft.preloadLockTicks = getWindup();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        ServerWorld world = (ServerWorld) attacker.getWorld();

        if (world.getRegistryKey().equals(JDimensionRegistry.AU_DIMENSION_KEY)) {
            // Logic for cancelling dimhop early, and generating failsafe data
            if (!(user instanceof ServerPlayerEntity serverPlayer)) return Set.of();

            boolean isStored = PastDimensions.tryExit(user, targets); // Should always be true

            if (!isStored) { // If not stored, force your way back
                BlockPos spawnPos = serverPlayer.getSpawnPointPosition(); // Prioritize spawn point
                // Use current position if all else fails
                if (spawnPos == null) spawnPos = serverPlayer.getBlockPos();
                PastDimensions.enqueue( new DimensionData(user, new Vec3d(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()), serverPlayer.getSpawnPointDimension()) );
            }

            return Set.of();
        }

        if (JCraft.auWorld == null) throw new IllegalStateException("Alternate Universe could not be found.");

        fixLightInAU(attacker, world, JCraft.auWorld);

        Set<LivingEntity> toHop = new HashSet<>(targets);
        toHop.add(user);
        int heightOffset = JCraft.auWorld.getHeight() - world.getHeight();
        for (LivingEntity entity : toHop)
            JCraft.dimensionHop(entity, heightOffset / 2);

        return targets;
    }

    static final boolean enableLightingFix = false;

    //todo: fix fixLightInAU() crashing the server repeatedly (its currently not called)
    @SuppressWarnings("DataFlowIssue") // There is no issue
    private static void fixLightInAU(D4CEntity attacker, ServerWorld world, ServerWorld auWorld) {
        ChunkPos origin = attacker.getChunkPos();

        // Lighting providers are too complicated, man. Wth
        // We got 2 providers, every provider has 2 storages and every storage has 2 storages.

        boolean someModMessedUpLight = true;
        ChunkToNibbleArrayMap<?>
                ogBlockLightStorage = null,
                ogUncachedBlockLightStorage = null,
                auBlockLightStorage = null,
                auUncachedBlockLightStorage = null,
                ogSkyLightStorage = null,
                ogUncachedSkyLightStorage = null,
                auSkyLightStorage = null,
                auUncachedSkyLightStorage = null;

        if (enableLightingFix) {
            LightingProvider ogLightingProvider = world.getLightingProvider();
            LightingProvider auLightingProvider = auWorld.getLightingProvider();

            ChunkLightProviderAccessor ogBlockLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) ogLightingProvider).getBlockLightProvider();
            ChunkLightProviderAccessor auBlockLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) auLightingProvider).getBlockLightProvider();
            ChunkLightProviderAccessor ogSkyLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) ogLightingProvider).getSkyLightProvider();
            ChunkLightProviderAccessor auSkyLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) auLightingProvider).getSkyLightProvider();

            LightStorageAccessor ogBlockLightStorage0 = ogBlockLightProvider == null ? null :
                    (LightStorageAccessor) ogBlockLightProvider.getLightStorage();
            LightStorageAccessor auBlockLightStorage0 = auBlockLightProvider == null ? null :
                    (LightStorageAccessor) auBlockLightProvider.getLightStorage();
            LightStorageAccessor ogSkyLightStorage0 = ogSkyLightProvider == null ? null :
                    (LightStorageAccessor) ogSkyLightProvider.getLightStorage();
            LightStorageAccessor auSkyLightStorage0 = auSkyLightProvider == null ? null :
                    (LightStorageAccessor) auSkyLightProvider.getLightStorage();

            // Whether some mod (like Starlight or Phosphor) overwrote the lighting system.
            // If so, our method of copying light data is not going to work.
            someModMessedUpLight = Stream.of(ogBlockLightStorage0, auBlockLightStorage0, ogSkyLightStorage0, auSkyLightStorage0)
                    .anyMatch(Objects::isNull);

            ogBlockLightStorage = someModMessedUpLight ? null : ogBlockLightStorage0.getStorage();
            ogUncachedBlockLightStorage = someModMessedUpLight ? null : ogBlockLightStorage0.getUncachedStorage();
            auBlockLightStorage = someModMessedUpLight ? null : auBlockLightStorage0.getStorage();
            auUncachedBlockLightStorage = someModMessedUpLight ? null : auBlockLightStorage0.getUncachedStorage();
            ogSkyLightStorage = someModMessedUpLight ? null : ogSkyLightStorage0.getStorage();
            ogUncachedSkyLightStorage = someModMessedUpLight ? null : ogSkyLightStorage0.getUncachedStorage();
            auSkyLightStorage = someModMessedUpLight ? null : auSkyLightStorage0.getStorage();
            auUncachedSkyLightStorage = someModMessedUpLight ? null : auSkyLightStorage0.getUncachedStorage();

            someModMessedUpLight |= Stream.of(ogBlockLightStorage, ogUncachedBlockLightStorage, auBlockLightStorage, auUncachedBlockLightStorage,
                            ogSkyLightStorage, ogUncachedSkyLightStorage, auSkyLightStorage, auUncachedBlockLightStorage)
                    .anyMatch(Objects::isNull);
        }

        for (int x = -3; x < 4; x++) {
            for (int z = -3; z < 4; z++) {
                int cX = origin.x + x;
                int cZ = origin.z + z;
                JCraft.preloadChunk(auWorld, cX, cZ);

                WorldChunk ogChunk = world.getChunk(cX, cZ);
                WorldChunk auChunk = auWorld.getChunk(cX, cZ);

                ChunkSection[] sections = ogChunk.getSectionArray();
                ChunkSection[] copies = IntStream.range(0, sections.length)
                        .mapToObj(i -> {
                            ChunkSection copy = new ChunkSection(world.getRegistryManager().get(RegistryKeys.BIOME));
                            PacketByteBuf serialized = PacketByteBufs.create();
                            sections[i].toPacket(serialized);
                            copy.readDataPacket(serialized);
                            return copy;
                        })
                        .toArray(ChunkSection[]::new);

                ChunkSection[] auSec = auChunk.getSectionArray();
                System.arraycopy(copies, 0, auSec, 0, Math.min(copies.length, auSec.length));

                // Copy light for every section.
                if (!someModMessedUpLight)
                    for (int y = auWorld.getBottomY(); y < auWorld.getTopY(); y += 16) {
                        long cPos = ChunkSectionPos.toLong(new BlockPos(cX * 16, y, cZ * 16));
                        ChunkNibbleArray a;
                        a = ogBlockLightStorage.get(cPos);
                        if (a != null) auBlockLightStorage.put(cPos, a);

                        a = ogUncachedBlockLightStorage.get(cPos);
                        if (a != null) auUncachedBlockLightStorage.put(cPos, a);

                        a = ogSkyLightStorage.get(cPos);
                        if (a != null) auSkyLightStorage.put(cPos, a);

                        a = ogUncachedSkyLightStorage.get(cPos);
                        if (a != null) auUncachedSkyLightStorage.put(cPos, a);
                    }
            }
        }

        // todo: use auWorld.getLightingProvider().doLightUpdates()?
        for (BlockPos pos : BlockPos.iterate(new BlockPos(origin.getStartX() - 3 * 16, world.getBottomY(), origin.getStartZ() - 3 * 16),
                new BlockPos(origin.getEndX() + 3 * 16, world.getTopY(), origin.getEndZ() + 3 * 16))) {
            auWorld.removeBlockEntity(pos); // Ensure the old one is gone.
            auWorld.getBlockEntity(pos); // Creates the BE if it does not yet exist while there should be one.

            // If some mod felt the need to overwrite the light system,
            // they have probably improved the efficiency of this method.
            // Thus, it should theoretically be fine to call this for every block.
            if (enableLightingFix && someModMessedUpLight) auWorld.getLightingProvider().checkBlock(pos);
        }
    }

    @Override
    protected @NonNull DimensionalHopMove getThis() {
        return this;
    }

    @Override
    public @NonNull DimensionalHopMove copy() {
        return copyExtras(new DimensionalHopMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
