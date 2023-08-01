package net.arna.jcraft.common.splatter;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.minecraft.util.Identifier;

@Getter
public enum SplatterType {
    BLOOD("blood.png"),
    ACID("acid.png");

    private final Identifier texture;

    SplatterType(String texture) {
        this.texture = JCraft.id("textures/effect/splatter/" + texture);
    }
}
