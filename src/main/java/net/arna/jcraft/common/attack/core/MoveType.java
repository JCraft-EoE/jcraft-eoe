package net.arna.jcraft.common.attack.core;

import lombok.Getter;
import net.arna.jcraft.common.util.CooldownType;
import net.minecraft.text.Text;

@Getter
public enum MoveType {
    LIGHT(CooldownType.STAND_LIGHT, "key.attack"),
    HEAVY(CooldownType.STAND_HEAVY),
    BARRAGE(CooldownType.STAND_BARRAGE),
    SPECIAL1(CooldownType.STAND_SP1),
    SPECIAL2(CooldownType.STAND_SP2),
    SPECIAL3(CooldownType.STAND_SP3),
    ULTIMATE(CooldownType.STAND_ULTIMATE),
    UTILITY(CooldownType.UTILITY);

    private final Text friendlyName;
    private final Text key;
    private final CooldownType defaultCooldownType;

    MoveType(CooldownType defaultCooldownType) {
        this(defaultCooldownType, null);
    }

    MoveType(CooldownType defaultCooldownType, String key) {
        friendlyName = Text.translatable("jcraft.movetype." + name().toLowerCase());
        this.key = Text.keybind(key == null ? "key.jcraft." + name().toLowerCase() : key);
        this.defaultCooldownType = defaultCooldownType;
    }
}
