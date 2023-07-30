package net.arna.jcraft.common.spec;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum SpecType {
    NONE(null),
    BRAWLER(new BrawlerSpec()),
    ANUBIS(new AnubisSpec());

    @Getter
    private final JCraftSpec spec;
    @Getter
    private final int id;
    @Getter
    private final String internalName;
    @Getter
    private final Text translatablename;

    @Getter(lazy = true)
    private static final List<SpecType> allSpecTypes = ImmutableList.copyOf(values());

    // has to return a new one every time
    public static JCraftSpec fromId(int id) {
        switch (id) {
            default -> {
                return null;
            }
            case 1 -> {
                return new BrawlerSpec();
            }
            case 2 -> {
                return new AnubisSpec();
            }
        }
    }

    SpecType(@Nullable JCraftSpec spec) {
        this.spec = spec;
        if (spec != null) {
            this.id = spec.getId();
            this.internalName = spec.getInternalName();
            this.translatablename = spec.getTranslatableName();
        } else {
            this.id = 0;
            this.internalName = "none";
            this.translatablename = Text.of("none");
        }
    }
}
