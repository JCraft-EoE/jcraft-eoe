package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.tickable.PastDimensions;
import net.arna.jcraft.mixin.ChunkLightProviderAccessor;
import net.arna.jcraft.mixin.LightStorageAccessor;
import net.arna.jcraft.mixin.LightingProviderAccessor;
import net.arna.jcraft.api.registry.JDimensionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class DimensionalHopMove extends AbstractSimpleAttack<DimensionalHopMove, D4CEntity> {

    @Getter
    private final int dimensionalHopDuration;

    public DimensionalHopMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                              final float damage, final int stun, final float hitboxSize, final float knockback, final float offset,
                              final int dimensionalHopDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        if (dimensionalHopDuration < 0) {
            throw new IllegalArgumentException("dimensionHopTime cannot be negative!");
        }
        this.dimensionalHopDuration = dimensionalHopDuration;
    }

    @Override
    public @NotNull MoveType<DimensionalHopMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean conditionsMet(D4CEntity attacker) {
        return super.conditionsMet(attacker) || attacker.level().dimension().equals(JDimensionRegistry.AU_DIMENSION_KEY);
    }

    @Override
    public void onInitiate(final D4CEntity attacker) {
        super.onInitiate(attacker);

        if (attacker.level() != JCraft.auWorld) {
            JCraft.auWorld.getChunkSource().addRegionTicket(TicketType.PORTAL, attacker.chunkPosition(), -5, attacker.blockPosition());
            JCraft.preloadLockTicks = getWindup();
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final D4CEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        if (dimensionalHopDuration == 0) {
            return targets;
        }

        final ServerLevel world = (ServerLevel) attacker.level();

        if (world.dimension().equals(JDimensionRegistry.AU_DIMENSION_KEY)) {
            // Logic for cancelling dimhop early, and generating failsafe data
            if (!(user instanceof ServerPlayer serverPlayer)) {
                return Set.of();
            }

            boolean isStored = PastDimensions.tryExit(user, targets); // Should always be true
            if (!isStored) { // If not stored, force your way back
                PastDimensions.safeReturn(serverPlayer);
            }

            return Set.of();
        }

        if (JCraft.auWorld == null) {
            throw new IllegalStateException("Alternate Universe could not be found.");
        }

        fixLightInAU(attacker, world, JCraft.auWorld);

        Set<LivingEntity> toHop = new HashSet<>(targets);
        toHop.add(user);
        int heightOffset = JCraft.auWorld.getHeight() - world.getHeight();
        for (LivingEntity entity : toHop) {
            JCraft.dimensionHop(entity, heightOffset / 2, dimensionalHopDuration);
        }

        return targets;
    }

    static final boolean enableLightingFix = false;

    //todo: fix fixLightInAU() crashing the server repeatedly (its currently not called)
    @SuppressWarnings("DataFlowIssue") // There is no issue
    private static void fixLightInAU(final D4CEntity attacker, final ServerLevel world, final ServerLevel auWorld) {
        ChunkPos origin = attacker.chunkPosition();

        // Lighting providers are too complicated, man. Wth
        // We got 2 providers, every provider has 2 storages and every storage has 2 storages.

        boolean someModMessedUpLight = true;
        DataLayerStorageMap<?>
                ogBlockLightStorage = null,
                ogUncachedBlockLightStorage = null,
                auBlockLightStorage = null,
                auUncachedBlockLightStorage = null,
                ogSkyLightStorage = null,
                ogUncachedSkyLightStorage = null,
                auSkyLightStorage = null,
                auUncachedSkyLightStorage = null;

        if (enableLightingFix) {
            LevelLightEngine ogLightingProvider = world.getLightEngine();
            LevelLightEngine auLightingProvider = auWorld.getLightEngine();

            ChunkLightProviderAccessor ogBlockLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) ogLightingProvider).getBlockEngine();
            ChunkLightProviderAccessor auBlockLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) auLightingProvider).getBlockEngine();
            ChunkLightProviderAccessor ogSkyLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) ogLightingProvider).getSkyEngine();
            ChunkLightProviderAccessor auSkyLightProvider = (ChunkLightProviderAccessor)
                    ((LightingProviderAccessor) auLightingProvider).getSkyEngine();

            LightStorageAccessor ogBlockLightStorage0 = ogBlockLightProvider == null ? null :
                    (LightStorageAccessor) ogBlockLightProvider.getStorage();
            LightStorageAccessor auBlockLightStorage0 = auBlockLightProvider == null ? null :
                    (LightStorageAccessor) auBlockLightProvider.getStorage();
            LightStorageAccessor ogSkyLightStorage0 = ogSkyLightProvider == null ? null :
                    (LightStorageAccessor) ogSkyLightProvider.getStorage();
            LightStorageAccessor auSkyLightStorage0 = auSkyLightProvider == null ? null :
                    (LightStorageAccessor) auSkyLightProvider.getStorage();

            // Whether some mod (like Starlight or Phosphor) overwrote the lighting system.
            // If so, our method of copying light data is not going to work.
            someModMessedUpLight = Stream.of(ogBlockLightStorage0, auBlockLightStorage0, ogSkyLightStorage0, auSkyLightStorage0)
                    .anyMatch(Objects::isNull);

            ogBlockLightStorage = someModMessedUpLight ? null : ogBlockLightStorage0.getUpdatingSectionData();
            ogUncachedBlockLightStorage = someModMessedUpLight ? null : ogBlockLightStorage0.getVisibleSectionData();
            auBlockLightStorage = someModMessedUpLight ? null : auBlockLightStorage0.getUpdatingSectionData();
            auUncachedBlockLightStorage = someModMessedUpLight ? null : auBlockLightStorage0.getVisibleSectionData();
            ogSkyLightStorage = someModMessedUpLight ? null : ogSkyLightStorage0.getUpdatingSectionData();
            ogUncachedSkyLightStorage = someModMessedUpLight ? null : ogSkyLightStorage0.getVisibleSectionData();
            auSkyLightStorage = someModMessedUpLight ? null : auSkyLightStorage0.getUpdatingSectionData();
            auUncachedSkyLightStorage = someModMessedUpLight ? null : auSkyLightStorage0.getVisibleSectionData();

            someModMessedUpLight |= Stream.of(ogBlockLightStorage, ogUncachedBlockLightStorage, auBlockLightStorage, auUncachedBlockLightStorage,
                            ogSkyLightStorage, ogUncachedSkyLightStorage, auSkyLightStorage, auUncachedBlockLightStorage)
                    .anyMatch(Objects::isNull);
        }

        for (int x = -3; x < 4; x++) {
            for (int z = -3; z < 4; z++) {
                int cX = origin.x + x;
                int cZ = origin.z + z;
                JCraft.preloadChunk(auWorld, cX, cZ);

                LevelChunk ogChunk = world.getChunk(cX, cZ);
                LevelChunk auChunk = auWorld.getChunk(cX, cZ);

                LevelChunkSection[] sections = ogChunk.getSections();
                LevelChunkSection[] copies = IntStream.range(0, sections.length)
                        .mapToObj(i -> {
                            LevelChunkSection copy = new LevelChunkSection(world.registryAccess().registryOrThrow(Registries.BIOME));
                            FriendlyByteBuf serialized = new FriendlyByteBuf(Unpooled.buffer());
                            sections[i].write(serialized);
                            copy.read(serialized);
                            return copy;
                        })
                        .toArray(LevelChunkSection[]::new);

                LevelChunkSection[] auSec = auChunk.getSections();
                System.arraycopy(copies, 0, auSec, 0, Math.min(copies.length, auSec.length));

                // Copy light for every section.
                if (!someModMessedUpLight) {
                    for (int y = auWorld.getMinBuildHeight(); y < auWorld.getMaxBuildHeight(); y += 16) {
                        long cPos = SectionPos.asLong(new BlockPos(cX * 16, y, cZ * 16));
                        DataLayer a;
                        a = ogBlockLightStorage.getLayer(cPos);
                        if (a != null) {
                            auBlockLightStorage.setLayer(cPos, a);
                        }

                        a = ogUncachedBlockLightStorage.getLayer(cPos);
                        if (a != null) {
                            auUncachedBlockLightStorage.setLayer(cPos, a);
                        }

                        a = ogSkyLightStorage.getLayer(cPos);
                        if (a != null) {
                            auSkyLightStorage.setLayer(cPos, a);
                        }

                        a = ogUncachedSkyLightStorage.getLayer(cPos);
                        if (a != null) {
                            auUncachedSkyLightStorage.setLayer(cPos, a);
                        }
                    }
                }
            }
        }

        // todo: use auWorld.getLightingProvider().doLightUpdates()?
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(origin.getMinBlockX() - 3 * 16, world.getMinBuildHeight(), origin.getMinBlockZ() - 3 * 16),
                new BlockPos(origin.getMaxBlockX() + 3 * 16, world.getMaxBuildHeight(), origin.getMaxBlockZ() + 3 * 16))) {
            auWorld.removeBlockEntity(pos); // Ensure the old one is gone.
            auWorld.getBlockEntity(pos); // Creates the BE if it does not yet exist while there should be one.

            if (auWorld.getBlockState(pos).is(JTagRegistry.AU_REPLACED_WITH_AIR)) {
                auWorld.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }

            // If some mod felt the need to overwrite the light system,
            // they have probably improved the efficiency of this method.
            // Thus, it should theoretically be fine to call this for every block.
            if (enableLightingFix && someModMessedUpLight) {
                auWorld.getLightEngine().checkBlock(pos);
            }
        }
    }

    @Override
    protected @NonNull DimensionalHopMove getThis() {
        return this;
    }

    @Override
    public @NonNull DimensionalHopMove copy() {
        return copyExtras(new DimensionalHopMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getDimensionalHopDuration()));
    }

    public static class Type extends AbstractSimpleAttack.Type<DimensionalHopMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<DimensionalHopMove>, DimensionalHopMove> buildCodec(RecordCodecBuilder.Instance<DimensionalHopMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("dimensionalHopDuration").forGetter(DimensionalHopMove::getDimensionalHopDuration)).apply(instance, DimensionalHopMove::new);
        }
    }
}
