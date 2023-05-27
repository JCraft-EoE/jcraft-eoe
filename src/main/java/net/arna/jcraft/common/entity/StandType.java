package net.arna.jcraft.common.entity;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public enum StandType {
    STAR_PLATINUM(JEntityTypeRegister.STAR_PLATINUM, StarPlatinumEntity::new, "starplatinum"),                  // 1
    THE_WORLD(JEntityTypeRegister.THE_WORLD, TheWorldEntity::new, "theworld"),                                  // 2
    KING_CRIMSON(JEntityTypeRegister.KING_CRIMSON, KingCrimsonEntity::new, "kingcrimson"),                      // 3
    D4C(JEntityTypeRegister.D4C, D4CEntity::new, "d4c"),                                                        // 4
    CREAM(JEntityTypeRegister.CREAM, CreamEntity::new, "cream"),                                                // 5
    KILLER_QUEEN(JEntityTypeRegister.KILLER_QUEEN, KillerQueenEntity::new, "killerqueen"),                      // 6
    WHITE_SNAKE(JEntityTypeRegister.WHITE_SNAKE, WhitesnakeEntity::new, "whitesnake"),                          // 7
    SILVER_CHARIOT(JEntityTypeRegister.SILVER_CHARIOT, SilverChariotEntity::new, "silverchariot"),              // 8
    MAGICIANS_RED(JEntityTypeRegister.MAGICIANS_RED, MagiciansRedEntity::new, "mr"),                            // 9
    THE_FOOL(JEntityTypeRegister.THE_FOOL, TheFoolEntity::new, "thefool"),                                      // 10
    GOLDEN_EXPERIENCE(JEntityTypeRegister.GOLDEN_EXPERIENCE, GoldenExperienceEntity::new, "goldenexperience"),  // 11

    // Evolutions
    C_MOON(JEntityTypeRegister.C_MOON, CMoonEntity::new, "cmoon", true),                                  // -1
    MADE_IN_HEAVEN(JEntityTypeRegister.MADE_IN_HAVEN, MadeInHeavenEntity::new, "mih", true),                // -2
    THE_WORLD_OVER_HEAVEN(JEntityTypeRegister.THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenEntity::new, "twoh", true),  // -3
    KILLER_QUEEN_BITES_THE_DUST(JEntityTypeRegister.KILLER_QUEEN_BITES_THE_DUST, KQBTDEntity::new, "kqbtd",true),  // -4
    GER(JEntityTypeRegister.GER, GEREntity::new, "ger", true);                                              // -5


    @Getter(lazy = true)
    private static final List<StandType> regularStandTypes = Arrays.stream(values()).filter(t -> t.getId() > 0).collect(ImmutableList.toImmutableList());
    @Getter(lazy = true)
    private static final List<StandType> evoStandTypes = Arrays.stream(values()).filter(t -> t.getId() < 0).collect(ImmutableList.toImmutableList());
    @Getter(lazy = true)
    private static final List<StandType> allStandTypes = ImmutableList.copyOf(values());

    @Getter
    private final EntityType<? extends StandEntity> entityType;
    @Getter
    private final int id;
    private final BiFunction<EntityType<? extends StandEntity>, ServerWorld, StandEntity> ctor;
    @Getter
    private final Text nameText;

    StandType(EntityType<? extends StandEntity> entityType, BiFunction<EntityType<? extends StandEntity>, ServerWorld, StandEntity> ctor, String nameKey) {
        this(entityType, ctor, nameKey, false);
    }

    StandType(EntityType<? extends StandEntity> entityType, BiFunction<EntityType<? extends StandEntity>, ServerWorld, StandEntity> ctor, String nameKey, boolean isEvo) {
        this.entityType = entityType;
        id = isEvo ? --StaticFields.nextEvoId : ++StaticFields.nextId;
        this.ctor = ctor;
        this.nameText = Text.translatable("entity.jcraft." + nameKey);

        StaticFields.fromId.put(id, this);
    }

    @Nullable
    public static StandType fromId(int internalId) {
        return StaticFields.fromId.get(internalId);
    }

    @NonNull
    public StandEntity createNew(ServerWorld world) {
        return ctor.apply(getEntityType(), world);
    }

    // Can't access static fields in enum constructor, blah blah blah.
    private static class StaticFields {
        private static final Int2ObjectMap<StandType> fromId = new Int2ObjectOpenHashMap<>(16); // Increase this number when adding more stands.
        private static int nextId, nextEvoId;
    }
}
