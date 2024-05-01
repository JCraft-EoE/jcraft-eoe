package net.arna.jcraft.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import lombok.Synchronized;
import net.arna.jcraft.JCraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JServerConfig {
    // TODO fix the default values
    // Balance options
    private static final String BALANCE = "balance";
    public static final IntOption SPTW_TIME_STOP_DURATION = new IntOption("sptwTimeStopDuration", BALANCE, 35, 0);
    public static final IntOption TW_TIME_STOP_DURATION = new IntOption("twTimeStopDuration", BALANCE, 80, 0);
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
    public static final FloatOption DAMAGE_SCALING_MINIMUM = new FloatOption("damageScalingMinimum", BALANCE, 0.4f);
    public static final FloatOption SCALING_PENALTY_PER_HIT = new FloatOption("scalingPenaltyPerHit", BALANCE, 0.02f);
    public static final BooleanOption ENABLE_MOVE_COOLDOWNS = new BooleanOption("enableMoveCooldowns", BALANCE, true);
    public static final FloatOption COOLDOWN_MULTIPLIER = new FloatOption("cooldownMultiplier", BALANCE, 1.0f);
    public static final BooleanOption ENABLE_IPS = new BooleanOption("enableIPS", BALANCE, false);
    public static final BooleanOption ENABLE_FRIENDLY_FIRE = new BooleanOption("enableFriendlyFire", BALANCE, true);

    private static final String MINECRAFT_REBALANCE = "minecraft_rebalance";
    public static final BooleanOption REDUCE_DEADLY_EXPLOSIONS = new BooleanOption("reduceDeadlyExplosions", MINECRAFT_REBALANCE, true);

    // Interaction options
    private static final String INTERACTION = "interaction";
    /*
    public static final BooleanOption UNIVERSAL_ABILITIES = new BooleanOption("universalAbilities", INTERACTION, true);
    public static final BooleanOption EXCLUSIVE_STANDS = new BooleanOption("exclusiveStands", INTERACTION, false);
    public static final BooleanOption BARRAGE_MINING = new BooleanOption("barrageMining", INTERACTION, false);
    public static final FloatOption BARRAGE_MINING_SPEED = new FloatOption("barrageMiningSpeed", INTERACTION, 1f, 0f, 10f);
    public static final BooleanOption STAND_GRIEFING = new BooleanOption("standGriefing", INTERACTION, true);
    public static final BooleanOption SPTW_IGNITE_CAMPFIRES = new BooleanOption("sptwIgniteCampfires", INTERACTION, true);
    public static final BooleanOption WS_STEAL_STANDS = new BooleanOption("wsStealStands", INTERACTION, false);
    public static final IntOption SHA_SEARCH_RADIUS = new IntOption("shaSearchRadius", INTERACTION, 10, 3, 32);
    public static final BooleanOption MIH_ACCELERATE_TICKS = new BooleanOption("mihAccelerateTicks", INTERACTION, true);
    public static final BooleanOption USE_FOOLISH_SAND = new BooleanOption("useFoolishSand", INTERACTION, true);
     */

    // TODO list options
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Empty method to force class initialization.
    // Not doing this breaks the /jconfig command (cuz this class won't be initialized on clients).
    public static void init() {}

    @SneakyThrows
    public static void load(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("jcraft.json");
        if (!Files.exists(path)) {
            save(server);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            for (String key : data.keySet()) {
                ConfigOption option = ConfigOption.getImmutableOptions().get(key);
                if (option != null) option.read(data.get(key));
            }
        } catch (IOException e) {
            JCraft.LOGGER.error("An error occurred trying to read the server config.", e);
        }
    }

    @Synchronized
    @SneakyThrows
    public static void save(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("jcraft.json");

        JsonObject data = new JsonObject();
        ConfigOption.getImmutableOptions().forEach((key, option) -> data.add(key, option.write()));

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(data, writer);
        }
    }
}
