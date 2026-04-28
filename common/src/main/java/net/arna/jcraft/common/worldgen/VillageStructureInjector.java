package net.arna.jcraft.common.worldgen;

import com.mojang.datafixers.util.Pair;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.mixin.StructureTemplatePoolAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects jcraft structures (currently the boxing arena) into vanilla village
 * template pools at server start.
 *
 * <p>Vanilla villages are jigsaw structures composed of pieces drawn from
 * {@code minecraft:village/<type>/houses} pools. Rather than overriding those
 * pool JSONs (which would conflict with any other mod that does the same), we
 * append our own pool element to them in-place once the registries have been
 * loaded from data packs.
 */
public final class VillageStructureInjector {
    /**
     * Weight of the boxing arena entry inside each village houses pool.
     * Higher = more common. Vanilla houses typically sit between 2-10.
     */
    private static final int BOXING_ARENA_WEIGHT = 1;

    /**
     * Identifier of the boxing arena structure NBT, relative to
     * {@code data/jcraft/structures/}.
     */
    private static final ResourceLocation BOXING_ARENA_LOCATION =
            JCraft.id("boxing_arena/boxing_arena");

    /**
     * Vanilla village biomes whose houses pool should be augmented.
     */
    private static final List<String> VILLAGE_TYPES = List.of(
            "plains",
            "desert",
            "savanna",
            "snowy",
            "taiga"
    );

    private VillageStructureInjector() {}

    /**
     * Called at {@code LifecycleEvent.SERVER_BEFORE_START}; by that point the
     * dynamic registries (template pools, processor lists, ...) have been loaded
     * from data packs but no chunks have been generated yet.
     */
    public static void onServerBeforeStart(final MinecraftServer server) {
        final Registry<StructureTemplatePool> poolRegistry =
                server.registryAccess().registry(Registries.TEMPLATE_POOL).orElse(null);
        if (poolRegistry == null) {
            JCraft.LOGGER.warn("[VillageStructureInjector] Template pool registry not available - boxing arena will not generate in villages.");
            return;
        }

        final Registry<StructureProcessorList> processorRegistry =
                server.registryAccess().registry(Registries.PROCESSOR_LIST).orElse(null);
        if (processorRegistry == null) {
            JCraft.LOGGER.warn("[VillageStructureInjector] Processor list registry not available - boxing arena will not generate in villages.");
            return;
        }

        // Use the empty processor list so the structure is placed verbatim.
        final Holder<StructureProcessorList> emptyProcessors = processorRegistry.getHolderOrThrow(
                ResourceKey.create(Registries.PROCESSOR_LIST, new ResourceLocation("minecraft", "empty"))
        );

        // Build a single-piece pool element pointing at our NBT.
        // Using `RIGID` so the structure is placed exactly as authored
        // (doesn't try to terrain-match to a sloped surface).
        final StructurePoolElement element = StructurePoolElement
                .single(BOXING_ARENA_LOCATION.toString(), emptyProcessors)
                .apply(StructureTemplatePool.Projection.RIGID);

        int injected = 0;
        for (final String type : VILLAGE_TYPES) {
            final ResourceLocation poolLocation =
                    new ResourceLocation("minecraft", "village/" + type + "/houses");
            final StructureTemplatePool pool = poolRegistry.get(poolLocation);
            if (pool == null) {
                JCraft.LOGGER.warn("[VillageStructureInjector] Missing village pool: {}", poolLocation);
                continue;
            }
            inject(pool, element, BOXING_ARENA_WEIGHT);
            injected++;
        }

        JCraft.LOGGER.info("[VillageStructureInjector] Boxing arena injected into {} village pool(s).", injected);
    }

    /**
     * Append {@code element} to the given pool with the given weight.
     * <p>
     * The expanded {@code templates} list contains the element {@code weight}
     * times (one entry per weight unit, so the weighted random pick works
     * out of the box). The {@code rawTemplates} list stores the (element,
     * weight) pair, kept in sync for serialization correctness.
     */
    private static void inject(final StructureTemplatePool pool,
                               final StructurePoolElement element,
                               final int weight) {
        final StructureTemplatePoolAccessor accessor = (StructureTemplatePoolAccessor) pool;

        final List<StructurePoolElement> templates = accessor.jcraft$getTemplates();
        for (int i = 0; i < weight; i++) {
            templates.add(element);
        }

        // rawTemplates is an ImmutableList, so we need to make it mutable.
        // Field is final, so we used an access widener to make the field mutable.
        ArrayList<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(accessor.jcraft$getRawTemplates());
        rawTemplates.add(Pair.of(element, weight));
        accessor.jcraft$setRawTemplates(rawTemplates);
    }
}
