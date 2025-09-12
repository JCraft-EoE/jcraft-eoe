package net.arna.jcraft.api.stand;

import lombok.experimental.UtilityClass;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.common.data.AttackerDataLoader;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.arna.jcraft.JCraft.ALLOW_MOB_EVOLVED_STANDS;

@UtilityClass
public class StandTypeUtil {
    /**
     * Checks whether the given stand type is {@code null} or the {@code NONE} stand type.
     * @param type The type to check for
     * @return Whether the given type is equivalent to the {@code NONE} type.
     */
    public static boolean isNone(final StandType type) {
        return type == null || type == JStandTypeRegistry.NONE.get();
    }

    /**
     * Creates a stream of all stand types, including unobtainable ones.
     * @return a stream of all stand types
     */
    public static Stream<StandType> streamAll() {
        return StreamSupport.stream(JRegistries.STAND_TYPE_REGISTRY.spliterator(), false);
    }

    /**
     * Like {@link #streamAll()}, but filters out unobtainable stands.
     * @return a stream of all obtainable stands.
     */
    public static Stream<StandType> streamAllObtainable() {
        return streamAll().filter(type -> type.getData().isObtainable());
    }

    /**
     * Creates a stream of all regular (non-evolution) stand types.
     * @return a stream of all regular stand types.
     */
    public static Stream<StandType> streamAllRegular() {
        return streamAllObtainable().filter(type -> !type.getData().isEvolution());
    }

    /**
     * Creates a stream of all evolution stand types.
     * @return a stream of all evolution stand types.
     */
    public static Stream<StandType> streamAllEvolutions() {
        return streamAllObtainable().filter(type -> type.getData().isEvolution());
    }

    /**
     * Creates a stream of each stand type's entity type.
     * @return a stream of each stand type's entity type.
     */
    public static Stream<EntityType<? extends StandEntity<?, ?>>> streamEntityTypes() {
        return streamAll().map(StandType::getEntityType);
    }

    /**
     * Gets a random regular (so no evolutions) stand type.
     * @return a random regular stand type.
     */
    public static StandType getRandomRegular(final RandomSource random) {
        final List<StandType> types = streamAllObtainable()
                .filter(type -> !type.getData().isEvolution())
                .toList();
        return types.get(random.nextInt(types.size()));
    }

    /**
     * Gets a random obtainable stand type, may return an evolution type.
     * @return a random stand type.
     */
    public static StandType getRandom(final RandomSource random) {
        final List<StandType> types = streamAllObtainable().toList();
        return types.get(random.nextInt(types.size()));
    }

    /**
     * Reads a stand type from the given NBT compound with the given key.
     * First attempts to read a legacy integer ordinal, then a string ID.
     * @param nbt The NBT compound to read from
     * @param key The key to read the stand type from
     * @return the stand type, or {@code null} if not found or an invalid type was found.
     */
    @Nullable
    public static StandType readFromNBT(final CompoundTag nbt, final String key) {
        if (nbt.contains(key, Tag.TAG_INT)) {
            int ordinal = nbt.getInt(key);
            return Optional.ofNullable(JStandTypeRegistry.LEGACY_ORDINALS.get(ordinal))
                    .map(Supplier::get)
                    .orElse(null);
        } else if (nbt.contains(key, Tag.TAG_STRING)) {
            String id = nbt.getString(key);
            if (id.isEmpty()) {
                return null;
            }
            return JRegistries.STAND_TYPE_REGISTRY.get(new ResourceLocation(id));
        } else {
            return null;
        }
    }

    /**
     * Gets the stand data with the given id. Used for stands that have multiple.
     * (I.e. a different StandData based on the stand's state, see
     * {@link net.arna.jcraft.common.entity.stand.SilverChariotEntity Silver Chariot})
     */
    public static StandData getStandData(final ResourceLocation id) {
        return AttackerDataLoader.getStandData(id);
    }

    public static StandType generateStandTypeForMob(Entity entity, GameRules gameRules) {
        return gameRules.getBoolean(ALLOW_MOB_EVOLVED_STANDS)
                ? getRandom(entity.level().random)
                : getRandomRegular(entity.level().random);
    }
}
