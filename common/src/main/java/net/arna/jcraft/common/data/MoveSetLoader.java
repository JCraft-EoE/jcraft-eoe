package net.arna.jcraft.common.data;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MoveSetLoader {
    private static final Map<ResourceLocation, Map<String, Collection<Pair<ResourceLocation, JsonObject>>>> PENDING_MOVE_SET_DATA = new HashMap<>();

    public static void attemptLoad(final MoveSet<?, ?> moveSet) {
        // This method is called when a MoveSet is created.
        // It will load the move set data if it exists.
        final ResourceLocation typeLoc = moveSet.getType().getId();
        final String moveSetName = moveSet.getName();

        Map<String, Collection<Pair<ResourceLocation, JsonObject>>> typeMoveSets = PENDING_MOVE_SET_DATA
                .getOrDefault(typeLoc, Collections.emptyMap());
        if (typeMoveSets.containsKey(moveSetName)) {
            JCraft.LOGGER.warn("Moveset '{}' for attacker {} was not instantiated until after data was loaded. " +
                    "This is deprecated behaviour. Please ensure all moveset instances have been instantiated " +
                    "by the time data is loaded.", moveSetName, typeLoc);
            moveSet.load(JsonOps.INSTANCE, typeMoveSets.get(moveSetName), null);
            typeMoveSets.remove(moveSetName);

            if (typeMoveSets.isEmpty()) {
                PENDING_MOVE_SET_DATA.remove(typeLoc);
            }
        }
    }

    /**
     * Called upon datapack (re)load.
     * Loads stand movesets from datapacks.
     * @param preparationBarrier Preparation stuff that must be finished before reading anything
     * @param resourceManager The resource manager used to get data
     * @param preparationsProfiler Profiler for preparations
     * @param reloadProfiler Profiler for reload
     * @param backgroundExecutor Executor for background tasks
     * @param gameExecutor Executor for game tasks
     * @return A completable future that completes when the reload is done
     * @see net.minecraft.server.ReloadableServerResources
     */
    public static CompletableFuture<Void> onReload(final PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                   final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler,
                                                   final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> loadFiles(resourceManager), backgroundExecutor)
                .thenCompose(preparationBarrier::wait) // Wait for preparations to finish
                .thenAcceptAsync(data -> loadMoveSets(data, gameExecutor));
    }

    /**
     * Loads/reloads all move sets using the given resource manager.
     *
     * @param resourceManager The resource manager to load from.
     * @return A map of move sets with attacker keys and move set values
     */
    private static Map<ResourceLocation, Multimap<String, Pair<ResourceLocation, JsonObject>>> loadFiles(final ResourceManager resourceManager) {
        final Map<ResourceLocation, Resource> resources = resourceManager.listResources("movesets",
                rl -> rl.getPath().split("/").length >= 4 && rl.getPath().endsWith(".json"));

        final Gson gson = new Gson();
        // Load resources into JsonObjects and order them by attacker type and move set name.
        final Map<ResourceLocation, Multimap<String, Pair<ResourceLocation, JsonObject>>> moveSets = new HashMap<>();
        for (final Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            final String[] parts = entry.getKey().getPath().split("/");
            final ResourceLocation typeLoc = entry.getKey().withPath(parts[2]); // e.g. "jcraft:star_platinum"
            final String moveSetName = parts[3];

            final Multimap<String, Pair<ResourceLocation, JsonObject>> map = moveSets.computeIfAbsent(typeLoc,
                    s -> MultimapBuilder.hashKeys(2).arrayListValues().build());

            // Read resource into a JsonObject.
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                final JsonObject obj = gson.fromJson(reader, JsonObject.class);
                map.put(moveSetName, Pair.of(entry.getKey(), obj));
            } catch (final IOException e) {
                JCraft.LOGGER.error("Failed to load move {} for move set {} of type {}",
                        entry.getKey(), moveSetName, typeLoc, e);
            }
        }

        return moveSets;
    }

    /**
     * Loads all move sets from the given map.
     * This method is called after the files are loaded and the preparation barrier is released.
     *
     * @param moveSets The map of move sets to load.
     * @param gameExecutor The executor for game tasks.
     */
    private static void loadMoveSets(final Map<ResourceLocation, Multimap<String, Pair<ResourceLocation, JsonObject>>> moveSets, final Executor gameExecutor) {
        PENDING_MOVE_SET_DATA.clear();

        for (final Map.Entry<ResourceLocation, Multimap<String, Pair<ResourceLocation, JsonObject>>> typeEntry : moveSets.entrySet()) {
            final ResourceLocation typeLoc = typeEntry.getKey();
            final Multimap<String, Pair<ResourceLocation, JsonObject>> sets = typeEntry.getValue();

            for (final Map.Entry<String, Collection<Pair<ResourceLocation, JsonObject>>> moveSetEntry : sets.asMap().entrySet()) {
                final String moveSetName = moveSetEntry.getKey();
                final MoveSet<?, ?> moveSet = MoveSetManager.get(typeLoc, moveSetName);
                if (moveSet == null) {
                    // Either this moveset does not exist or the class of the stand entity it corresponds to is not loaded yet.
                    // We cannot be sure which one it is, so we just store this moveset for later.
                    PENDING_MOVE_SET_DATA.computeIfAbsent(typeLoc, k -> new HashMap<>())
                            .put(moveSetName, moveSetEntry.getValue());
                    continue;
                }

                moveSet.load(JsonOps.INSTANCE, moveSetEntry.getValue(), gameExecutor);
            }
        }
    }
}
