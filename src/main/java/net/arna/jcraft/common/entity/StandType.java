package net.arna.jcraft.common.entity;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public enum StandType {
    STAR_PLATINUM(JEntityTypeRegister.STAR_PLATINUM, StarPlatinumEntity::new, "starplatinum",                   // 1
            Text.literal("Manga"), Text.literal("Arcade"), Text.literal("OVA")),
    THE_WORLD(JEntityTypeRegister.THE_WORLD, TheWorldEntity::new, "theworld",                                   // 2
            Text.literal("Arcade"), Text.literal("Manga"), Text.literal("OVA")),
    KING_CRIMSON(JEntityTypeRegister.KING_CRIMSON, KingCrimsonEntity::new, "kingcrimson",                       // 3
            Text.literal("Royal"), Text.literal("Manga"), Text.literal("Concept")),
    D4C(JEntityTypeRegister.D4C, D4CEntity::new, "d4c",                                                         // 4
            Text.literal("Pride"), Text.literal("Europe"), Text.literal("Spangled")),
    CREAM(JEntityTypeRegister.CREAM, CreamEntity::new, "cream",                                                 // 5
            Text.literal("Menace"), Text.literal("Eraser"), Text.literal("White Void")),
    KILLER_QUEEN(JEntityTypeRegister.KILLER_QUEEN, KillerQueenEntity::new, "killerqueen",                       // 6
            Text.literal("Gunpowder"), Text.literal("Deadly"), Text.literal("1999")),
    WHITE_SNAKE(JEntityTypeRegister.WHITE_SNAKE, WhiteSnakeEntity::new, "whitesnake",                           // 7
            Text.literal("Mamba"), Text.literal("Kingsnake"), Text.literal("Melting")),
    SILVER_CHARIOT(JEntityTypeRegister.SILVER_CHARIOT, SilverChariotEntity::new, "silverchariot",               // 8
            Text.literal("Gold Chariot"), Text.literal("OVA"), Text.literal("Vento")),
    MAGICIANS_RED(JEntityTypeRegister.MAGICIANS_RED, MagiciansRedEntity::new, "mr",                             // 9
            Text.literal("Purple"), Text.literal("Moltres"), Text.literal("Fried")),
    THE_FOOL(JEntityTypeRegister.THE_FOOL, TheFoolEntity::new, "thefool",                                       // 10
            Text.literal("Chilled"), Text.literal("OVA"), Text.literal("Manga")),
    GOLD_EXPERIENCE(JEntityTypeRegister.GOLD_EXPERIENCE, GoldenExperienceEntity::new, "goldenexperience",       // 11
            Text.literal("Passione"), Text.literal("Chosen One"), Text.literal("Life Energy")),

    // Evolutions
    C_MOON(JEntityTypeRegister.C_MOON, CMoonEntity::new, "cmoon", true,                                             // -1
            Text.literal("Inversion"), Text.literal("Gravity"), Text.literal("Pale")),
    MADE_IN_HEAVEN(JEntityTypeRegister.MADE_IN_HEAVEN, MadeInHeavenEntity::new, "mih", true,                        // -2
            Text.literal("Pony"), Text.literal("Daft"), Text.literal("Nightmare")),
    THE_WORLD_OVER_HEAVEN(JEntityTypeRegister.THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenEntity::new, "twoh", true,   // -3
            Text.literal("Shadow"), Text.literal("Gone to Heaven"), Text.literal("Greatest High")),
    KILLER_QUEEN_BITES_THE_DUST(JEntityTypeRegister.KILLER_QUEEN_BITES_THE_DUST, KQBTDEntity::new, "kqbtd",true,    // -4
            Text.literal("Gelatin"), Text.literal("Veiled"), Text.literal("Garf")),
    GOLD_EXPERIENCE_REQUIEM(JEntityTypeRegister.GER, GEREntity::new, "ger", true,                                   // -5
            Text.literal("Energized"), Text.literal("Manga"), Text.literal("Silver")),
    STAR_PLATINUM_THE_WORLD(JEntityTypeRegister.SPTW, SPTWEntity::new, "sptw", true,                                // -6
            Text.literal("Judge, Jury, Executioner"), Text.literal("Diamond"), Text.literal("Over Heaven"));


    @Getter(lazy = true)
    private static final List<StandType> regularStandTypes = Arrays.stream(values()).filter(t -> !t.isEvolution()).collect(ImmutableList.toImmutableList());
    @Getter(lazy = true)
    private static final List<StandType> evoStandTypes = Arrays.stream(values()).filter(StandType::isEvolution).collect(ImmutableList.toImmutableList());
    @Getter(lazy = true)
    private static final List<StandType> allStandTypes = ImmutableList.copyOf(values());

    @Getter
    private static final int regularStandCount = getRegularStandTypes().size(), evoStandCount = getEvoStandTypes().size(),
            totalStandCount = regularStandCount + evoStandCount;

    @Getter
    private final EntityType<? extends StandEntity<?, ?>> entityType;
    @Getter
    private final int id;
    private final Function<World, StandEntity<?, ?>> ctor;
    @Getter
    private final Text nameText;
    @Getter
    private final List<Text> skinNames;

    StandType(EntityType<? extends StandEntity<?, ?>> entityType, Function<World, StandEntity<?, ?>> ctor, String nameKey, Text... skinNames) {
        this(entityType, ctor, nameKey, false, skinNames);
    }

    StandType(EntityType<? extends StandEntity<?, ?>> entityType, Function<World, StandEntity<?, ?>> ctor, String nameKey, boolean isEvo, Text... skinNames) {
        this.entityType = entityType;
        id = isEvo ? --StaticFields.nextEvoId : ++StaticFields.nextId;
        this.ctor = ctor;
        this.nameText = Text.translatable("entity.jcraft." + nameKey);
        this.skinNames = ImmutableList.copyOf(skinNames);

        StaticFields.fromId.put(id, this);
    }

    @Nullable
    public static StandType fromId(int internalId) {
        return StaticFields.fromId.get(internalId);
    }

    @NonNull
    public StandEntity<?, ?> createNew(World world) {
        return ctor.apply(world);
    }

    public boolean isEvolution() {
        return id < 0;
    }

    public int getSkinCount() {
        return skinNames.size();
    }

    // Can't access static fields in enum constructor, blah blah blah.
    private static class StaticFields {
        private static final Int2ObjectMap<StandType> fromId = new Int2ObjectOpenHashMap<>(17); // Increase this number when adding more stands.
        private static int nextId, nextEvoId;
    }
}
