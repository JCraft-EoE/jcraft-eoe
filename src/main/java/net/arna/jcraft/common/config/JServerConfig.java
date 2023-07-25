package net.arna.jcraft.common.config;

import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import net.arna.jcraft.common.spec.SpecType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JServerConfig {
    // TODO fix the default values
    // Balance options
    private static final String BALANCE = "balance";
    public static final IntOption SPTW_TIME_STOP_DURATION = new IntOption("sptwTimeStopDuration", BALANCE, 100, 0);
    public static final IntOption TW_TIME_STOP_DURATION = new IntOption("twTimeStopDuration", BALANCE, 100, 0);
    public static final IntOption TWOH_TIME_STOP_DURATION = new IntOption("twohTimeStopDuration", BALANCE, 100, 0);
    public static final IntOption MIH_TIME_ACCELERATION_DURATION = new IntOption("mihTimeAccelerationDuration", BALANCE, 300, 0);
    public static final IntOption KC_TIME_ERASURE_DURATION = new IntOption("kcTimeErasureDuration", BALANCE, 300, 0);
    public static final IntOption CMOON_UTIL_DURATION = new IntOption("cmoonUtilDuration", BALANCE, 300, 0);
    public static final FloatOption STAND_DAMAGE_MULTIPLIER = new FloatOption("standDamageMultiplier", BALANCE, 1f, 0f, 5f);
    public static final IntOption CMOON_ULT_RANGE = new IntOption("cmoonUltRange", BALANCE, 100, 0, 256);
    public static final EnumOption<SpecType> DEF_SPEC = new EnumOption<>("defSpec", BALANCE, SpecType.class, SpecType.BRAWLER); // TODO a NONE SpecType instead of null?
    public static final BooleanOption IGNORE_ARMOR = new BooleanOption("ignoreArmor", BALANCE, true);
    public static final BooleanOption INVIS_CREAM_VOID = new BooleanOption("invisCreamVoid", BALANCE, false);
    public static final BooleanOption TIME_SKIP_USE_UTIL = new BooleanOption("timeSkipUseUtil", BALANCE, false);

    // Interaction options
    private static final String INTERACTION = "interaction";
    public static final BooleanOption UNIVERSAL_ABILITIES = new BooleanOption("universalAbilities", INTERACTION, true);
    public static final BooleanOption EXCLUSIVE_STANDS = new BooleanOption("exclusiveStands", INTERACTION, false);
    public static final BooleanOption BARRAGE_MINING = new BooleanOption("barrageMining", INTERACTION, false);
    public static final FloatOption BARRAGE_MINING_SPEED = new FloatOption("barrageMiningSpeed", INTERACTION, 1f, 0f, 10f);
    public static final BooleanOption STAND_GRIEFING = new BooleanOption("standGriefing", INTERACTION, true);
    public static final BooleanOption SPTW_IGNITE_CAMPFIRES = new BooleanOption("sptwIgniteCampfires", INTERACTION, true);
    // TODO list options
    public static final BooleanOption WS_STEAL_STANDS = new BooleanOption("wsStealStands", INTERACTION, false);
    public static final IntOption SHA_SEARCH_RADIUS = new IntOption("shaSearchRadius", INTERACTION, 10, 3, 32);
    public static final BooleanOption MIH_ACCELERATE_TICKS = new BooleanOption("mihAccelerateTicks", INTERACTION, true);
    public static final BooleanOption USE_FOOLISH_SAND = new BooleanOption("useFoolishSand", INTERACTION, true);

    @SneakyThrows
    public static void load(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("jcraft.dat");
        if (!Files.exists(path)) return;

        byte[] data = Files.readAllBytes(path);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
        ConfigOption.readOptions(buf);
    }

    @SneakyThrows
    public static void save(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("jcraft.dat");
        PacketByteBuf buf = PacketByteBufs.create();
        ConfigOption.writeOptions(buf, ConfigOption.getImmutableOptions().values());

        byte[] bytes = new byte[buf.writerIndex()];
        buf.getBytes(0, bytes);
        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
