package net.arna.jcraft.common.util;

import net.arna.jcraft.common.attack.core.MoveInputType;

import java.util.EnumMap;

public class InputMap extends EnumMap<MoveInputType, Integer> {
    public InputMap() {
        super(MoveInputType.class);
    }

    public EnumMap<MoveInputType, Integer> cloneOf() {
        return super.clone();
    }
}
