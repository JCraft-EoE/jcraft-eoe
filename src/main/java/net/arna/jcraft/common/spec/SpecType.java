package net.arna.jcraft.common.spec;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.minecraft.text.Text;

import java.util.List;

public enum SpecType {
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

    SpecType(JCraftSpec spec) {
        this.spec = spec;
        this.id = spec.getId();
        this.internalName = spec.getInternalName();
        this.translatablename = spec.getTranslatableName();
    }
}
