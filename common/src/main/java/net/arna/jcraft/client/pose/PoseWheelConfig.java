package net.arna.jcraft.client.pose;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.arna.jcraft.JCraft;
import dev.architectury.platform.Platform;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists the 9-slot wheel layout to config/jcraft_pose_wheel.json.
 * Each slot stores the windupAnimId of the assigned pose, or null if empty.
 */
public class PoseWheelConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int SLOTS = 9;

    // null = empty slot
    private static final String[] slots = new String[SLOTS];

    @Nullable
    public static String getSlot(int index) {
        if (index < 0 || index >= SLOTS) return null;
        return slots[index];
    }

    public static void setSlot(int index, @Nullable String animId) {
        if (index < 0 || index >= SLOTS) return;
        slots[index] = animId;
    }

    @Nullable
    public static PoseDefinition getPoseAt(int index) {
        String id = getSlot(index);
        if (id == null) return null;
        for (PoseDefinition def : PoseLoader.getPoses()) {
            if (def.windupAnimId().equals(id)) return def;
        }
        return null;
    }

    public static void save() {
        try {
            Path path = configPath();
            JsonObject root = new JsonObject();
            for (int i = 0; i < SLOTS; i++) {
                root.addProperty("slot_" + i, slots[i]);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            JCraft.LOGGER.warn("PoseWheelConfig: failed to save", e);
        }
    }

    public static void load() {
        try {
            Path path = configPath();
            if (!Files.exists(path)) return;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) return;
                for (int i = 0; i < SLOTS; i++) {
                    var el = root.get("slot_" + i);
                    slots[i] = (el != null && !el.isJsonNull()) ? el.getAsString() : null;
                }
            }
        } catch (Exception e) {
            JCraft.LOGGER.warn("PoseWheelConfig: failed to load", e);
        }
    }

    private static Path configPath() {
        return Platform.getConfigFolder().resolve("jcraft_pose_wheel.json");
    }
}
