package net.arna.jcraft.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.serverconfig.*;
import net.arna.jcraft.common.ai.IJAttackerBrain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class JServerConfig {
    public static final Codec<IntOption> INT_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (IntOption) ConfigOption.getOption(parseKey(s)),
                    () -> "Unknown option: "), o -> o.getKey().toString());
    public static final Codec<FloatOption> FLOAT_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (FloatOption) ConfigOption.getOption(parseKey(s)),
                    () -> "Unknown option: "), o -> o.getKey().toString());
    public static final Codec<BooleanOption> BOOLEAN_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (BooleanOption) ConfigOption.getOption(parseKey(s)),
                    () -> "Unknown option: "), o -> o.getKey().toString());
    public static final Codec<EnumOption<?>> ENUM_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (EnumOption<?>) ConfigOption.getOption(parseKey(s)),
                    () -> "Unknown option: "), o -> o.getKey().toString());

    private static final Pattern CC_TO_SC1 = Pattern.compile("([a-z\\d])([A-Z])");
    private static final Pattern CC_TO_SC2 = Pattern.compile("([A-Z]+)([A-Z][a-z])");

    // ---------------------FOR ADDON DEVS:-------------------------
    // If you want to add your own options, do NOT try to use the categories JCraft options use.
    // Instead, create a separate category specifically for the options your addon adds.

    // Balance options
    private static final ResourceLocation BALANCE = JCraft.id("balance");
    public static final IntOption SPTW_TIME_STOP_DURATION = new IntOption(JCraft.id("sptw_time_stop_duration"), BALANCE, 35, 0);
    public static final IntOption TW_TIME_STOP_DURATION = new IntOption(JCraft.id("tw_time_stop_duration"), BALANCE, 80, 0);
    public static final IntOption STW_TIME_STOP_DURATION = new IntOption(JCraft.id("stw_time_stop_duration"), BALANCE, 50, 0);
    public static final IntOption TWOH_TIME_STOP_DURATION = new IntOption(JCraft.id("twoh_time_stop_duration"), BALANCE, 100, 0);
    public static final IntOption MIH_TIME_ACCELERATION_DURATION = new IntOption(JCraft.id("mih_time_acceleration_duration"), BALANCE, 300, 0);
    public static final BooleanOption KILL_VAMPIRISM = new BooleanOption(JCraft.id("kill_vampirism"), BALANCE, false);
    /*
    public static final IntOption KC_TIME_ERASURE_DURATION = new IntOption(JCraft.id("kc_time_erasure_duration"), BALANCE, 120, 0);
    public static final IntOption CMOON_UTIL_DURATION = new IntOption(JCraft.id("cmoon_util_duration"), BALANCE, 300, 0);
    public static final FloatOption STAND_DAMAGE_MULTIPLIER = new FloatOption(JCraft.id("stand_damage_multiplier"), BALANCE, 1f, 0f, 5f);
    public static final IntOption CMOON_ULT_RANGE = new IntOption(JCraft.id("cmoon_ult_range"), BALANCE, 100, 0, 256);
    public static final EnumOption<SpecType> DEF_SPEC = new EnumOption<>("def_spec", BALANCE, SpecType.class, SpecType.NONE);
    public static final BooleanOption IGNORE_ARMOR = new BooleanOption(JCraft.id("ignore_armor"), BALANCE, true);
    public static final BooleanOption INVIS_CREAM_VOID = new BooleanOption(JCraft.id("invis_cream_void"), BALANCE, false);
    public static final BooleanOption TIME_SKIP_USE_UTIL = new BooleanOption(JCraft.id("time_skip_use_util"), BALANCE, false);
     */
    public static final BooleanOption HEALTH_TO_DAMAGE_SCALING = new BooleanOption(JCraft.id("health_to_damage_scaling"), BALANCE, true);
    public static final FloatOption VS_STANDLESS_DAMAGE_MULTIPLIER = new FloatOption(JCraft.id("vs_standless_damage_multiplier"), BALANCE, 1.5f);
    public static final FloatOption DAMAGE_SCALING_MINIMUM = new FloatOption(JCraft.id("damage_scaling_minimum"), BALANCE, 0.4f);
    public static final FloatOption SCALING_PENALTY_PER_HIT = new FloatOption(JCraft.id("scaling_penalty_per_hit"), BALANCE, 0.02f);
    public static final BooleanOption ENABLE_MOVE_COOLDOWNS = new BooleanOption(JCraft.id("enable_move_cooldowns"), BALANCE, true);
    public static final FloatOption COOLDOWN_MULTIPLIER = new FloatOption(JCraft.id("cooldown_multiplier"), BALANCE, 1.0f);
    // public static final BooleanOption ENABLE_IPS = new BooleanOption(JCraft.id("enable_ips"), BALANCE, false);
    public static final BooleanOption SURVIVAL_CDC = new BooleanOption(JCraft.id("survival_cdc"), BALANCE, false);
    public static final BooleanOption ENABLE_FRIENDLY_FIRE = new BooleanOption(JCraft.id("enable_friendly_fire"), BALANCE, true);
    public static final IntOption BASE_AI_LEVEL = new IntOption(JCraft.id("base_ai_level"), BALANCE, IJAttackerBrain.COMPETITIVE_LEVEL, IJAttackerBrain.MIN_LEVEL, IJAttackerBrain.MAX_LEVEL);

    private static final ResourceLocation MINECRAFT_REBALANCE = JCraft.id("minecraft_rebalance");
    public static final BooleanOption REDUCE_DEADLY_EXPLOSIONS = new BooleanOption(JCraft.id("reduce_deadly_explosions"), MINECRAFT_REBALANCE, true);
    public static final BooleanOption DISABLE_COMBAT_ELYTRA = new BooleanOption(JCraft.id("disable_combat_elytra"), MINECRAFT_REBALANCE, true);

    // Interaction options
    private static final ResourceLocation INTERACTION = JCraft.id("interaction");
    public static final BooleanOption MINING_BARRAGE = new BooleanOption(JCraft.id("mining_barrage"), INTERACTION, false);
    public static final FloatOption METEOR_SPAWN_RATE = new FloatOption(JCraft.id("meteor_spawn_rate"), INTERACTION, 0.02f, 0f, 1f);
    public static final FloatOption STAND_ARROW_SPAWN_RATE = new FloatOption(JCraft.id("stand_arrow_spawn_rate"), INTERACTION, 0.01f, 0f, 1f);
    public static final IntOption DUMMY_DAMAGE_INDICATOR_RANGE = new IntOption(JCraft.id("dummy_damage_indicator_range"), INTERACTION, 64, 0, 512);
    public static final BooleanOption CREAM_ITEM_ERASE = new BooleanOption(JCraft.id("cream_item_erase"), INTERACTION, true);
    public static final BooleanOption ROLLER_FLATTENING = new BooleanOption(JCraft.id("roller_flattening"), INTERACTION, true);
    public static final BooleanOption ROLLER_DESTROYING = new BooleanOption(JCraft.id("roller_destroying"), INTERACTION, true);
    public static final BooleanOption PLAYER_VAMPS_DIE_TO_HAMON = new BooleanOption(JCraft.id("player_vamps_die_to_hamon"), INTERACTION, true);
    public static final BooleanOption MANDOM_AFFECTS_BLOCKS = new BooleanOption(JCraft.id("mandom_affects_blocks"), INTERACTION, true);
    public static final BooleanOption WS_STEAL_STANDS_FROM_PLAYERS = new BooleanOption(JCraft.id("ws_steal_stands_from_players"), INTERACTION, false);
    public static final BooleanOption GRAVITY_ONLY_AFFECTS_PLAYERS = new BooleanOption(JCraft.id("gravity_only_affects_players"), INTERACTION, false);
    public static final FloatOption BLOCK_BREAKAGE_MULTIPLIER = new FloatOption(JCraft.id("block_breakage_multiplier"), INTERACTION, 1.0f);
    /*
    public static final BooleanOption UNIVERSAL_ABILITIES = new BooleanOption(JCraft.id("universal_abilities"), INTERACTION, true);
    public static final BooleanOption STAND_GRIEFING = new BooleanOption(JCraft.id("stand_griefing"), INTERACTION, true);
    public static final BooleanOption SPTW_IGNITE_CAMPFIRES = new BooleanOption(JCraft.id("sptw_ignite_campfires"), INTERACTION, true);
    public static final IntOption SHA_SEARCH_RADIUS = new IntOption(JCraft.id("sha_search_radius"), INTERACTION, 10, 3, 32);
    public static final BooleanOption MIH_ACCELERATE_TICKS = new BooleanOption(JCraft.id("mih_accelerate_ticks"), INTERACTION, true);
    public static final BooleanOption USE_FOOLISH_SAND = new BooleanOption(JCraft.id("use_foolish_sand"), INTERACTION, true);
     */

    // Gameplay options
    private static final ResourceLocation GAMEPLAY = JCraft.id("gameplay");
    // public static final BooleanOption ENABLE_HITSTOP = new BooleanOption(JCraft.id("enable_hitstop"), GAMEPLAY, false);
    public static final BooleanOption EXCLUSIVE_STANDS = new BooleanOption(JCraft.id("exclusive_stands"), GAMEPLAY, false);
    public static final BooleanOption STAND_USER_SIGHT = new BooleanOption(JCraft.id("stand_user_sight"), GAMEPLAY, false);
    public static final BooleanOption SPAWNER_STANDS = new BooleanOption(JCraft.id("spawner_stands"), GAMEPLAY, true);

    // TODO list options
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Path GLOBAL_DEFAULT = Path.of("./config/jconfig.json");

    // Empty method to force class initialization.
    // Not doing this breaks the /jconfig command (cuz this class won't be initialized on clients).
    public static void init() {
        // intentionally left empty
    }

    @SneakyThrows
    public static void load(final MinecraftServer server) {
        Path path = getPath(server);

        if (!Files.exists(path)) {
            // No config file yet, check if there's a default in the config folder.
            Path defaultPath = GLOBAL_DEFAULT;
            if (Files.exists(defaultPath)) {
                // There's a default config file, copy it to the world folder and load it instead.
                Files.copy(defaultPath, path, StandardCopyOption.REPLACE_EXISTING);
            } else {
                save(server);

                // No need to load anything, we're using the defaults.
                return;
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            for (String key : data.keySet()) {
                ConfigOption option = ConfigOption.getOptions().get(parseKey(key));
                if (option != null) {
                    option.read(data.get(key));
                }
            }
        } catch (IOException e) {
            JCraft.LOGGER.error("An error occurred trying to read the server config.", e);
        }
    }

    @Synchronized
    @SneakyThrows
    public static void save(final MinecraftServer server) {
        Path path = getPath(server);

        JsonObject data = new JsonObject();
        ConfigOption.getOptions().forEach((key, option) ->
                data.add(key.toString(), option.write()));

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(data, writer);
            writer.flush();
        }
    }

    private static @NotNull Path getPath(final MinecraftServer server) throws IOException {
        // Try to read from world directory.
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("jcraft.json");

        // On dedicated servers, the preferred location is the config directory.
        if (server.isDedicatedServer()) {
            Path newPath = GLOBAL_DEFAULT;
            if (Files.exists(path)) {
                // If the old path exists, move the file.
                JCraft.LOGGER.warn("Moving jcraft.json to config directory.");
                Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
            }

            path = newPath;
        }
        return path;
    }

    @NonNull
    public static FriendlyByteBuf writeOptions(final @NonNull FriendlyByteBuf buf, final Collection<ConfigOption> options) {
        for (ConfigOption option : options) {
            buf.writeResourceLocation(option.getKey());
            option.write(buf);
        }

        return buf;
    }

    @NonNull
    public static Set<ConfigOption> readOptions(final FriendlyByteBuf buf) {
        Set<ConfigOption> changedOptions = new HashSet<>();
        while (buf.readableBytes() > 0) {
            ResourceLocation key = buf.readResourceLocation();
            ConfigOption option = ConfigOption.getOption(key);
            if (option == null) {
                JCraft.LOGGER.warn("Could not find option {}. Rest of the data ({} bytes) will be ignored.",
                        key, buf.readableBytes());

                buf.readerIndex(buf.readerIndex() + buf.readableBytes()); // Move cursor to end.
                break; // Rest will be invalid
            }

            option.read(buf);
            changedOptions.add(option);
        }
        return changedOptions;
    }

    private static ResourceLocation parseKey(String key) {
        // Convert camelCase to snake_case.
        // Config options used to use camelCase for naming, but resource locations only support snake case.
        key = CC_TO_SC1.matcher(key).replaceAll(r -> r.group(1) + "_" + r.group(2));
        key = CC_TO_SC2.matcher(key).replaceAll(r -> r.group(1) + "_" + r.group(2));
        key = key.toLowerCase(Locale.ROOT);

        // Default to jcraft namespace if no namespace is provided
        return key.indexOf(':') == -1 ? JCraft.id(key) : new ResourceLocation(key);
    }
}
