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
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ServerJoinData extends SavedData {

    public static final Path DEFAULT_FILE_LOCATION = new File("data", "jcraft_serverJoined.dat").toPath();
    protected static final CompoundTag JOINED_PLAYERS = new CompoundTag();
    protected static final String KEYWORD = "joined";

    protected final List<UUID> joinedPlayers = new LinkedList<>();

    public ServerJoinData(final @NotNull CompoundTag compoundTag) {
        Tag tag = compoundTag.get(KEYWORD);
        if (tag instanceof ListTag list) {
            joinedPlayers.addAll(list.stream()
                    .map(Tag::getAsString)
                    .map(UUID::fromString)
                    .toList());
        }
    }

    public boolean addPlayer(final ServerPlayer player) {
        if (player == null) {
            return false;
        }
        final UUID uuid = player.getUUID();
        if (joinedPlayers.contains(uuid)) {
            return false;
        }
        return joinedPlayers.add(uuid);
    }

    public static File getDefaultFileLocation(final MinecraftServer server) {
        return ((LevelStorageAccessAccessor) ((MinecraftServerAccessor) server).getStorageSource()).getLevelDirectory()
                .path().resolve(DEFAULT_FILE_LOCATION).toFile();
    }

    @Override
    @NonNull
    public CompoundTag save(final CompoundTag compoundTag) {
        final ListTag tag = new ListTag();
        tag.addAll(joinedPlayers.stream()
                .map(UUID::toString)
                .map(StringTag::valueOf)
                .toList());
        compoundTag.put(KEYWORD, tag);
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
