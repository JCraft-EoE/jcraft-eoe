package net.arna.jcraft.common.attack.core;

import lombok.Getter;
import net.arna.jcraft.common.util.CooldownType;
import net.minecraft.text.Text;

@Getter
public enum MoveType {
    LIGHT(CooldownType.STAND_LIGHT),
    HEAVY(CooldownType.STAND_HEAVY),
    BARRAGE(CooldownType.STAND_BARRAGE),
    SPECIAL1(CooldownType.STAND_SP1),
    SPECIAL2(CooldownType.STAND_SP2),
    SPECIAL3(CooldownType.STAND_SP3),
    ULTIMATE(CooldownType.ULTIMATE),
    UTILITY(CooldownType.UTILITY);

    private final Text friendlyName;
    private final CooldownType defaultCooldownType;

    MoveType(CooldownType defaultCooldownType) {
        friendlyName = Text.translatable("jcraft.movetype." + name().toLowerCase());
        this.defaultCooldownType = defaultCooldownType;
    }
}
