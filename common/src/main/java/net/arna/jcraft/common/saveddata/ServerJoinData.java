package net.arna.jcraft.common.saveddata;

import com.google.common.base.MoreObjects;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.mixin.LevelStorageAccessAccessor;
import net.arna.jcraft.mixin.MinecraftServerAccessor;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerJoinData extends SavedData {

    public static final Path DEFAULT_FILE_LOCATION = new File("data", "jcraft_serverJoined.dat").toPath();
    protected static final CompoundTag JOINED_PLAYERS = new CompoundTag();
    protected static final String KEYWORD_JOINED = "joined";
    protected static final String KEYWORD_UUID = "uuid";
    protected static final String KEYWORD_TIME = "time";

    protected final Map<UUID, Instant> joinedPlayers = new HashMap<>();

    public ServerJoinData(final @NotNull CompoundTag compoundTag) {
        Tag tag = compoundTag.get(KEYWORD_JOINED);
        if (tag instanceof final ListTag list) {
            joinedPlayers.putAll(list.stream()
                    .filter(CompoundTag.class::isInstance)
                    .map(CompoundTag.class::cast)
                    .filter(ct -> ct.contains(KEYWORD_UUID) && ct.contains(KEYWORD_TIME))
                    .collect(Collectors.toMap(
                            ct -> UUID.fromString(ct.getString(KEYWORD_UUID)),
                            ct -> Instant.ofEpochMilli(ct.getLong(KEYWORD_TIME))
                    )));
        }
    }

    public boolean addPlayer(final ServerPlayer player) {
        if (player == null) {
            return false;
        }
        final UUID uuid = player.getUUID();
        if (joinedPlayers.containsKey(uuid)) {
            return false;
        }
        joinedPlayers.put(uuid, Instant.now());
        setDirty();
        return true;
    }

    public static File getDefaultFileLocation(final MinecraftServer server) {
        return ((LevelStorageAccessAccessor) ((MinecraftServerAccessor) server).getStorageSource()).getLevelDirectory()
                .path().resolve(DEFAULT_FILE_LOCATION).toFile();
    }

    @Override
    @NonNull
    public CompoundTag save(final CompoundTag compoundTag) {
        final ListTag tag = new ListTag();
        tag.addAll(joinedPlayers.entrySet().stream()
                .map(entry -> {
                    final CompoundTag ct = new CompoundTag();
                    ct.putString(KEYWORD_UUID, entry.getKey().toString());
                    ct.putLong(KEYWORD_TIME, entry.getValue().getEpochSecond());
                    return ct;
                })
                .toList());
        compoundTag.put(KEYWORD_JOINED, tag);
        return compoundTag;
    }

    public void saveToDefaultFile(final MinecraftServer server) {
        save(getDefaultFileLocation(server));
    }

    public static ServerJoinData fromFile(final File file) {
        CompoundTag compoundTag = null;
        try {
            CompoundTag fileData = NbtIo.read(file);
            if (fileData != null)
                compoundTag = (CompoundTag) fileData.get("data");
        }
        catch (final NullPointerException | ClassCastException | IOException | ReportedException ex) {
            JCraft.LOGGER.warn(ex);
        }

        return new ServerJoinData(MoreObjects.firstNonNull(compoundTag, JOINED_PLAYERS));
    }

    public static ServerJoinData fromDefaultFile(final MinecraftServer server) {
        return fromFile(getDefaultFileLocation(server));
    }

}
