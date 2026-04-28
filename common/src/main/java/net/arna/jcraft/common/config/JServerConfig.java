package net.arna.jcraft.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.SneakyThrows;
import lombok.Synchronized;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.ai.IJAttackerBrain;
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

public class JServerConfig {
    public static final Codec<IntOption> INT_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (IntOption) ConfigOption.getOption(s, ConfigOption.Type.INTEGER), () -> "Unknown option: "),
            ConfigOption::getKey);
    public static final Codec<FloatOption> FLOAT_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (FloatOption) ConfigOption.getOption(s, ConfigOption.Type.FLOAT), () -> "Unknown option: "),
            ConfigOption::getKey);
    public static final Codec<BooleanOption> BOOLEAN_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (BooleanOption) ConfigOption.getOption(s, ConfigOption.Type.BOOLEAN), () -> "Unknown option: "),
            ConfigOption::getKey);
    public static final Codec<EnumOption<?>> ENUM_OPTION_CODEC = Codec.STRING.comapFlatMap(
            DataResult.partialGet(s -> (EnumOption<?>) ConfigOption.getOption(s, ConfigOption.Type.ENUM), () -> "Unknown option: "),
            ConfigOption::getKey);


    // TODO fix the default values
    // Balance options
    private static final String BALANCE = "balance";
    public static final IntOption SPTW_TIME_STOP_DURATION = new IntOption("sptwTimeStopDuration", BALANCE, 35, 0);
    public static final IntOption TW_TIME_STOP_DURATION = new IntOption("twTimeStopDuration", BALANCE, 80, 0);
    public static final IntOption STW_TIME_STOP_DURATION = new IntOption("stwTimeStopDuration", BALANCE, 50, 0);
    public static final IntOption TWOH_TIME_STOP_DURATION = new IntOption("twohTimeStopDuration", BALANCE, 100, 0);
    public static final IntOption MIH_TIME_ACCELERATION_DURATION = new IntOption("mihTimeAccelerationDuration", BALANCE, 300, 0);
    public static final BooleanOption KILL_VAMPIRISM = new BooleanOption("killVampirism", BALANCE, false);
    /*
    public static final IntOption KC_TIME_ERASURE_DURATION = new IntOption("kcTimeErasureDuration", BALANCE, 120, 0);
    public static final IntOption CMOON_UTIL_DURATION = new IntOption("cmoonUtilDuration", BALANCE, 300, 0);
    public static final FloatOption STAND_DAMAGE_MULTIPLIER = new FloatOption("standDamageMultiplier", BALANCE, 1f, 0f, 5f);
    public static final IntOption CMOON_ULT_RANGE = new IntOption("cmoonUltRange", BALANCE, 100, 0, 256);
    public static final EnumOption<SpecType> DEF_SPEC = new EnumOption<>("defSpec", BALANCE, SpecType.class, SpecType.NONE);
    public static final BooleanOption IGNORE_ARMOR = new BooleanOption("ignoreArmor", BALANCE, true);
    public static final BooleanOption INVIS_CREAM_VOID = new BooleanOption("invisCreamVoid", BALANCE, false);
    public static final BooleanOption TIME_SKIP_USE_UTIL = new BooleanOption("timeSkipUseUtil", BALANCE, false);
     */
    public static final BooleanOption HEALTH_TO_DAMAGE_SCALING = new BooleanOption("healthToDamageScaling", BALANCE, true);
    public static final FloatOption VS_STANDLESS_DAMAGE_MULTIPLIER = new FloatOption("vsStandlessDamageMultiplier", BALANCE, 1.5f);
    public static final FloatOption DAMAGE_SCALING_MINIMUM = new FloatOption("damageScalingMinimum", BALANCE, 0.4f);
    public static final FloatOption SCALING_PENALTY_PER_HIT = new FloatOption("scalingPenaltyPerHit", BALANCE, 0.02f);
    public static final BooleanOption ENABLE_MOVE_COOLDOWNS = new BooleanOption("enableMoveCooldowns", BALANCE, true);
    public static final FloatOption COOLDOWN_MULTIPLIER = new FloatOption("cooldownMultiplier", BALANCE, 1.0f);
    // public static final BooleanOption ENABLE_IPS = new BooleanOption("enableIPS", BALANCE, false);
    public static final BooleanOption SURVIVAL_CDC = new BooleanOption("survivalCDC", BALANCE, false);
    public static final BooleanOption ENABLE_FRIENDLY_FIRE = new BooleanOption("enableFriendlyFire", BALANCE, true);
    public static final IntOption BASE_AI_LEVEL = new IntOption("baseAILevel", BALANCE, IJAttackerBrain.COMPETITIVE_LEVEL, IJAttackerBrain.MIN_LEVEL, IJAttackerBrain.MAX_LEVEL);

    private static final String MINECRAFT_REBALANCE = "minecraft_rebalance";
    public static final BooleanOption REDUCE_DEADLY_EXPLOSIONS = new BooleanOption("reduceDeadlyExplosions", MINECRAFT_REBALANCE, true);
    public static final BooleanOption DISABLE_COMBAT_ELYTRA = new BooleanOption("disableCombatElytra", MINECRAFT_REBALANCE, true);

    // Interaction options
    private static final String INTERACTION = "interaction";
    public static final BooleanOption MINING_BARRAGE = new BooleanOption("miningBarrage", INTERACTION, true);
    public static final FloatOption METEOR_SPAWN_RATE = new FloatOption("meteorSpawnRate", INTERACTION, 0.02f, 0f, 1f);
    public static final FloatOption STAND_ARROW_SPAWN_RATE = new FloatOption("standArrowSpawnRate", INTERACTION, 0.01f, 0f, 1f);
    public static final IntOption DUMMY_DAMAGE_INDICATOR_RANGE = new IntOption("dummyDamageIndicatorRange", INTERACTION, 64, 0, 512);
    public static final BooleanOption CREAM_ITEM_ERASE = new BooleanOption("creamItemErase", INTERACTION, true);
    public static final BooleanOption ROLLER_FLATTENING = new BooleanOption("rollerFlattening", INTERACTION, true);
    public static final BooleanOption ROLLER_DESTROYING = new BooleanOption("rollerDestroying", INTERACTION, true);
    public static final BooleanOption PLAYER_VAMPS_DIE_TO_HAMON = new BooleanOption("playerVampsDieToHamon", INTERACTION, false);
    public static final BooleanOption MANDOM_AFFECTS_BLOCKS = new BooleanOption("mandomAffectsBlocks", INTERACTION, true);
    public static final BooleanOption WS_STEAL_STANDS_FROM_PLAYERS = new BooleanOption("wsStealStandsFromPlayers", INTERACTION, false);
    public static final BooleanOption GRAVITY_ONLY_AFFECTS_PLAYERS = new BooleanOption("gravityOnlyAffectsPlayers", INTERACTION, false);
    public static final BooleanOption SHOW_ALL_COSPLAY = new BooleanOption("showAllCosplay", INTERACTION, false);
    /*
    public static final BooleanOption UNIVERSAL_ABILITIES = new BooleanOption("universalAbilities", INTERACTION, true);
    public static final BooleanOption STAND_GRIEFING = new BooleanOption("standGriefing", INTERACTION, true);
    public static final BooleanOption SPTW_IGNITE_CAMPFIRES = new BooleanOption("sptwIgniteCampfires", INTERACTION, true);
    public static final IntOption SHA_SEARCH_RADIUS = new IntOption("shaSearchRadius", INTERACTION, 10, 3, 32);
    public static final BooleanOption MIH_ACCELERATE_TICKS = new BooleanOption("mihAccelerateTicks", INTERACTION, true);
    public static final BooleanOption USE_FOOLISH_SAND = new BooleanOption("useFoolishSand", INTERACTION, true);
     */

    // Misc options
    private static final String GAMEPLAY = "gameplay";
    // public static final BooleanOption ENABLE_HITSTOP = new BooleanOption("enableHitstop", GAMEPLAY, false);
    public static final BooleanOption EXCLUSIVE_STANDS = new BooleanOption("exclusiveStands", GAMEPLAY, false);
    public static final BooleanOption STAND_USER_SIGHT = new BooleanOption("standUserSight", GAMEPLAY, false);
    public static final BooleanOption SPAWNER_STANDS = new BooleanOption("spawnerStands", GAMEPLAY, true);

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
                ConfigOption option = ConfigOption.getImmutableOptions().get(key);
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
        ConfigOption.getImmutableOptions().forEach((key, option) -> data.add(key, option.write()));

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
}
