package net.arna.jcraft.common.splatter;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.minecraft.util.Identifier;

@Getter
public enum SplatterType {
    BLOOD("blood.png", 80),
    ACID("acid.png", 100);

    private final Identifier texture;
    private final int maxAge;

    SplatterType(String texture, int maxAge) {
        this.texture = JCraft.id("textures/effect/splatter/" + texture);
        this.maxAge = maxAge;
    }
}
