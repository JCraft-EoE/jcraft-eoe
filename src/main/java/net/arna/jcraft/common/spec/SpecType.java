package net.arna.jcraft.common.spec;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public enum SpecType {
    NONE(player -> null, Text.empty(), Text.empty()),
    BRAWLER(BrawlerSpec::new, Text.literal("Close-range pressure and combo extension tool"), Text.literal(
            """
                    Important hitconfirm: (any stand move)~stand.OFF>Combo>stand.ON+(any stand move)""")),
    ANUBIS(AnubisSpec::new, Text.literal("Accelerating offense"), Text.literal(
            """
                    PASSIVE: Bloodlust
                    Landing blows on opponents speeds up Anubis' attacks up to 2x, with +0.2x per hit.
                    Not hitting opponents reduces Bloodlust by one stack every 4 seconds.""")),

    VAMPIRE(VampireSpec::new, Text.literal("Supernatural all-ranger"), Text.literal(
            """
                    PASSIVES: Burns in sunlight, Blood replaces hunger, Night vision
                    Excellent frametraps with Sweep or Axe Kick.
                    Bloodsuck is extremely rewarding and allows linking into almost any move."""));

    @Getter(lazy = true)
    private static final List<SpecType> allSpecTypes = ImmutableList.copyOf(values());
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final Int2ObjectMap<SpecType> byId = getAllSpecTypes().stream()
            .collect(Int2ObjectOpenHashMap::new, (map, type) -> map.put(type.getId(), type), Int2ObjectMap::putAll);

    private final Function<PlayerEntity, @Nullable JSpec<?, ?>> specCreator;
    @Getter
    private final String internalName;
    @Getter
    private final Text translatableName, description, details;


    SpecType(Function<PlayerEntity, @Nullable JSpec<?, ?>> specCreator, Text description, Text details) {
        internalName = name().toLowerCase();
        translatableName = Text.translatable("spec.jcraft." + internalName);
        this.description = description;
        this.details = details;
        this.specCreator = specCreator;
    }

    public int getId() {
        return ordinal();
    }

    public JSpec<?, ?> createNew(PlayerEntity player) {
        return specCreator.apply(player);
    }

    public static SpecType fromId(int id) {
        return getById().get(id);
    }
}
