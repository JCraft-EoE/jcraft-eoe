package net.arna.jcraft.common.splatter;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

@Getter
public enum SplatterType {
    BLOOD("blood.png", 80),
    ACID("acid.png", 100),
    GASOLINE("gasoline.png", 300);

    private final ResourceLocation texture;
    private final int maxAge;

    SplatterType(String texture, int maxAge) {
        this.texture = JCraft.id("textures/effect/splatter/" + texture);
        this.maxAge = maxAge;
    }
}
