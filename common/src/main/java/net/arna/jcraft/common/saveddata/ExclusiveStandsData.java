package net.arna.jcraft.common.saveddata;

import com.google.common.base.MoreObjects;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.mixin.LevelStorageAccessAccessor;
import net.arna.jcraft.mixin.MinecraftServerAccessor;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.ReportedException;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

public class ExclusiveStandsData extends SavedData {
    public static final Path DEFAULT_FILE_LOCATION = new File("data", "jcraft_usedStands.dat").toPath();
    private static final CompoundTag NO_STANDS = new CompoundTag();
    private static final String KEYWORD = "used";
    private final Set<ResourceLocation> usedStands = new TreeSet<>();

    public ExclusiveStandsData(final @NotNull CompoundTag compoundTag) {
        Tag tag = compoundTag.get(KEYWORD);
        if (tag == null) return;

        // Legacy support for IntArrayTag
        if (tag.getType() == IntArrayTag.TYPE) {
            for (final IntTag i : ((IntArrayTag) tag)) {
                usedStands.add(JStandTypeRegistry.LEGACY_ORDINALS.get(i.getAsInt()).getId());
            }
        } else {
            usedStands.addAll(((ListTag) tag).stream()
                    .map(Tag::getAsString)
                    .map(ResourceLocation::new)
                    .toList());
        }
    }

    public boolean isStandUsed(final StandType standType) {
        return JServerConfig.EXCLUSIVE_STANDS.getValue() && usedStands.contains(standType.getId());
    }

    public boolean switchStand(final @Nullable StandType prev, final @Nullable StandType curr) {
        if (!JServerConfig.EXCLUSIVE_STANDS.getValue()) {
            return true;
        }

        // If the current stand is already used, return false.
        if (isStandUsed(curr)) {
            return false;
        }

        // If the previous stand is not null, remove it from the used stands.
        if (prev != null) {
            usedStands.remove(prev.getId());
        }
        // If the current stand is not null, add it to the used stands.
        if (curr != null) {
            usedStands.add(curr.getId());
        }
        setDirty();

        return true;
    }

    public static File getDefaultFileLocation(final MinecraftServer server) {
        return ((LevelStorageAccessAccessor) ((MinecraftServerAccessor) server).getStorageSource()).getLevelDirectory()
                .path().resolve(ExclusiveStandsData.DEFAULT_FILE_LOCATION).toFile();
    }

    @NotNull
    @Override
    public CompoundTag save(final CompoundTag compoundTag) {
        ListTag tag = new ListTag();
        tag.addAll(usedStands.stream()
                .map(ResourceLocation::toString)
                .map(StringTag::valueOf)
                .toList());
        compoundTag.put(KEYWORD, tag);
        return compoundTag;
    }

    public void saveToDefaultFile(final MinecraftServer server) {
        save(getDefaultFileLocation(server));
    }

    public static ExclusiveStandsData fromFile(final File file) {
        CompoundTag compoundTag = null;
        try {
            CompoundTag fileData = NbtIo.read(file);
            if (fileData != null)
                compoundTag = (CompoundTag) fileData.get("data");
        }
        catch (final NullPointerException | ClassCastException | IOException | ReportedException ex) {
            JCraft.LOGGER.warn(ex);
        }

        return new ExclusiveStandsData(MoreObjects.firstNonNull(compoundTag, NO_STANDS));
    }

    public static ExclusiveStandsData fromDefaultFile(final MinecraftServer server) {
        return fromFile(getDefaultFileLocation(server));
    }
}
