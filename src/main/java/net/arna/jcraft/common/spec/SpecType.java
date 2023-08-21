package net.arna.jcraft.common.spec;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public enum SpecType {
    NONE(() -> null),
    BRAWLER(BrawlerSpec::new),
    ANUBIS(AnubisSpec::new);

    @Getter(lazy = true)
    private static final List<SpecType> allSpecTypes = ImmutableList.copyOf(values());
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final Int2ObjectMap<SpecType> byId = getAllSpecTypes().stream()
            .collect(Int2ObjectOpenHashMap::new, (map, type) -> map.put(type.getId(), type), Int2ObjectMap::putAll);

    private final Supplier<@Nullable JSpec> specSupplier;
    @Getter
    private final int id;
    @Getter
    private final String internalName;
    @Getter
    private final Text translatableName;

    SpecType(Supplier<@Nullable JSpec> specSupplier) {
        this.specSupplier = specSupplier;

        JSpec spec = createNew();
        if (spec != null) {
            id = spec.getId();
            internalName = spec.getInternalName();
            translatableName = spec.getTranslatableName();
        } else {
            id = 0;
            internalName = "none";
            translatableName = Text.of("none");
        }
    }

    public JSpec createNew() {
        return specSupplier.get();
    }

    public static SpecType fromId(int id) {
        return getById().get(id);
    }
}
