package net.arna.jcraft.client.pose;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Scans assets/jcraft/player_animation/pose/*.json into PoseDefinitions on resource reload. */
public class PoseLoader implements PreparableReloadListener {

    public static final PoseLoader INSTANCE = new PoseLoader();

    @Getter
    private static List<PoseDefinition> poses = List.of();

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, @NotNull ResourceManager manager,
                                                   @NotNull ProfilerFiller prepareProfiler, @NotNull ProfilerFiller applyProfiler,
                                                   @NotNull Executor prepareExecutor, @NotNull Executor applyExecutor) {
        return CompletableFuture
                .supplyAsync(() -> load(manager), prepareExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(loaded -> poses = loaded, applyExecutor);
    }

    private static List<PoseDefinition> load(ResourceManager manager) {
        Map<ResourceLocation, Resource> resources = manager.listResources(
                "player_animation/pose",
                loc -> loc.getNamespace().equals(JCraft.MOD_ID) && loc.getPath().endsWith(".json")
        );

        List<PoseDefinition> result = new ArrayList<>();

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject animations = root.getAsJsonObject("animations");
                if (animations == null) continue;

                // Collect all animation names in this file
                Map<String, Float> lengths = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> anim : animations.entrySet()) {
                    String name = anim.getKey();
                    float length = 0f;
                    if (anim.getValue().isJsonObject()) {
                        JsonElement lenEl = anim.getValue().getAsJsonObject().get("animation_length");
                        if (lenEl != null) length = lenEl.getAsFloat();
                    }
                    lengths.put(name, length);
                }

                // Build PoseDefinitions for non-idle entries
                for (Map.Entry<String, Float> e : lengths.entrySet()) {
                    String name = e.getKey();
                    if (name.endsWith("_idle")) continue;

                    String idleId = name + "_idle";
                    String idleAnimId = lengths.containsKey(idleId) ? idleId : null;
                    int windupTicks = Math.max(1, (int) Math.ceil(e.getValue() * 20f));
                    String displayName = buildDisplayName(name);

                    result.add(new PoseDefinition(name, windupTicks, idleAnimId, displayName));
                }

            } catch (Exception e) {
                JCraft.LOGGER.warn("PoseLoader: failed to read {}", entry.getKey(), e);
            }
        }

        return Collections.unmodifiableList(result);
    }

    private static String buildDisplayName(String animId) {
        // "heritage_jotaro" -> "Heritage Jotaro"
        return StringUtils.capitalize(animId.replace('_', ' '));
    }
}
