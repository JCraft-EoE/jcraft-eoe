package net.arna.jcraft.common.entity.stand;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StandType {
    /**
     * NONE marks an entity as processed.
     * For players, this is functionally identical to null, for entities this disables obtaining a stand.
     */
    NONE(), // 255
    STAR_PLATINUM(JEntityTypeRegistry.STAR_PLATINUM, StarPlatinumEntity::new, "starplatinum",                   // 1
            Text.literal("Manga"), Text.literal("Arcade"), Text.literal("OVA")),
    THE_WORLD(JEntityTypeRegistry.THE_WORLD, TheWorldEntity::new, "theworld",                                   // 2
            Text.literal("OVA"), Text.literal("Black"), Text.literal("Greatest High")),
    KING_CRIMSON(JEntityTypeRegistry.KING_CRIMSON, KingCrimsonEntity::new, "kingcrimson",                       // 3
            Text.literal("Royal"), Text.literal("Manga"), Text.literal("Nightshade")),
    D4C(JEntityTypeRegistry.D4C, D4CEntity::new, "d4c",                                                         // 4
            Text.literal("Jojoveller"), Text.literal("Teaser"), Text.literal("Spangled")),
    CREAM(JEntityTypeRegistry.CREAM, CreamEntity::new, "cream",                                                 // 5
            Text.literal("Menace"), Text.literal("Eraser"), Text.literal("White Void")),
    KILLER_QUEEN(JEntityTypeRegistry.KILLER_QUEEN, KillerQueenEntity::new, "killerqueen",                       // 6
            Text.literal("Gunpowder"), Text.literal("Deadly"), Text.literal("1999")),
    WHITE_SNAKE(JEntityTypeRegistry.WHITE_SNAKE, WhiteSnakeEntity::new, "whitesnake",                           // 7
            Text.literal("Melting"), Text.literal("Mamba"), Text.literal("Redsnake")),
    SILVER_CHARIOT(JEntityTypeRegistry.SILVER_CHARIOT, SilverChariotEntity::new, "silverchariot",               // 8
            Text.literal("Gold Chariot"), Text.literal("OVA"), Text.literal("Vento")),
    MAGICIANS_RED(JEntityTypeRegistry.MAGICIANS_RED, MagiciansRedEntity::new, "mr",                             // 9
            Text.literal("Purple"), Text.literal("OVA"), Text.literal("Fried")),
    THE_FOOL(JEntityTypeRegistry.THE_FOOL, TheFoolEntity::new, "thefool",                                       // 10
            Text.literal("Chilled"), Text.literal("OVA"), Text.literal("Neon")),
    GOLD_EXPERIENCE(JEntityTypeRegistry.GOLD_EXPERIENCE, GoldExperienceEntity::new, "goldexperience",       // 11
            Text.literal("Anime"), Text.literal("Spectre"), Text.literal("Burning Passion")),
    HIEROPHANT_GREEN(JEntityTypeRegistry.HIEROPHANT_GREEN, HGEntity::new, "hierophantgreen",       // 12
            Text.literal("Cold"), Text.literal("Burning"), Text.literal("Seaside")),
    THE_SUN(JEntityTypeRegistry.THE_SUN, TheSunEntity::new, "the_sun",       // 13
            Text.literal(":D"), Text.literal("Neutron Star"), Text.literal("Dark")),
    PURPLE_HAZE(JEntityTypeRegistry.PURPLE_HAZE, PurpleHazeEntity::new, "purple_haze",       // 14
            Text.literal("Toxin"), Text.literal("Stopping Force"), Text.literal("Reversal")),


    // Evolutions
    C_MOON(JEntityTypeRegistry.C_MOON, CMoonEntity::new, "cmoon", true,                                             // -1
            Text.literal("Inversion"), Text.literal("Gravity"), Text.literal("Rose")),
    MADE_IN_HEAVEN(JEntityTypeRegistry.MADE_IN_HEAVEN, MadeInHeavenEntity::new, "mih", true,                        // -2
            Text.literal("Brick"), Text.literal("Daft"), Text.literal("Nightmare")),
    THE_WORLD_OVER_HEAVEN(JEntityTypeRegistry.THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenEntity::new, "twoh", true,   // -3
            Text.literal("Shooting Star"), Text.literal("Above the Clouds"), Text.literal("Dirt to Divinity")),
    KILLER_QUEEN_BITES_THE_DUST(JEntityTypeRegistry.KILLER_QUEEN_BITES_THE_DUST, KQBTDEntity::new, "kqbtd",true,    // -4
            Text.literal("Veiled"), Text.literal("Back from the Dead"), Text.literal("Garf")),
    GOLD_EXPERIENCE_REQUIEM(JEntityTypeRegistry.GER, GEREntity::new, "ger", true,                                   // -5
            Text.literal("Silver"), Text.literal("Manga"), Text.literal("Cherry Blossom")),
    STAR_PLATINUM_THE_WORLD(JEntityTypeRegistry.SPTW, SPTWEntity::new, "sptw", true,                                // -6
            Text.literal("Judge, Jury, Executioner"), Text.literal("Diamond"), Text.literal("Over Heaven")),
    PURPLE_HAZE_DISTORTION(JEntityTypeRegistry.PURPLE_HAZE_DISTORTION, PurpleHazeDistortionEntity::new, "purple_haze_distortion", true,       // -7
            Text.literal("Black Knight"), Text.literal("Vintage"), Text.literal("Reversal")),;


    @Getter(lazy = true)
    private static final List<StandType> regularStandTypes = Arrays.stream(values()).filter(t -> !t.isEvolution() && t != NONE).collect(ImmutableList.toImmutableList());
    @Getter(lazy = true)
    private static final List<StandType> evoStandTypes = Arrays.stream(values()).filter(t -> t.isEvolution() && t != NONE).collect(ImmutableList.toImmutableList());
    @Getter(lazy = true)
    private static final List<StandType> allStandTypes = Arrays.stream(values()).filter(t -> t != NONE).collect(ImmutableList.toImmutableList());

    @Getter
    private static final int regularStandCount = getRegularStandTypes().size(), evoStandCount = getEvoStandTypes().size(),
            totalStandCount = regularStandCount + evoStandCount;

    @Getter(lazy = true)
    private static final Set<EntityType<? extends StandEntity<?, ?>>> entityTypes = getAllStandTypes().stream()
            .map(StandType::getEntityType)
            .collect(Collectors.toSet());

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

    StandType() {
        this.entityType = null;
        this.id = 0xff; // ain't no way we make 255 regular stands
        this.ctor = null;
        this.nameText = Text.translatable("entity.jcraft.nostand");
        this.skinNames = List.of();

        StaticFields.fromId.put(0xff, this);
    }

    public static boolean isNone(StandType standType) {
        return standType == null || standType == NONE;
    }

    @Nullable
    public static StandType fromId(int internalId) {
        return StaticFields.fromId.get(internalId);
    }

    public static StandType getRandomRegular(Random random) {
        return getRegularStandTypes().get(random.nextInt(regularStandCount));
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
        private static final Int2ObjectMap<StandType> fromId = new Int2ObjectOpenHashMap<>(19); // Increase this number when adding more stands.
        private static int nextId, nextEvoId;
    }
}
