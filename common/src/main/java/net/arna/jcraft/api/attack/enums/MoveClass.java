package net.arna.jcraft.api.attack.enums;

import com.mojang.serialization.Codec;
import lombok.Getter;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JCodecUtils;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Random;

@Getter
public enum MoveClass {
    LIGHT(CooldownType.STAND_LIGHT, "key.attack"),
    HEAVY(CooldownType.STAND_HEAVY),
    BARRAGE(CooldownType.STAND_BARRAGE),
    SPECIAL1(CooldownType.STAND_SP1),
    SPECIAL2(CooldownType.STAND_SP2),
    SPECIAL3(CooldownType.STAND_SP3),
    ULTIMATE(CooldownType.STAND_ULTIMATE),
    UTILITY(CooldownType.UTILITY),
    TOSS(CooldownType.STAND_TOSS);

    public static final Codec<MoveClass> CODEC = JCodecUtils.createEnumCodec(MoveClass.class);
    private static final Random random = new Random();

    private final Component friendlyName;
    private final Component key;
    private final CooldownType defaultCooldownType;
    @Getter
    private final String name = name().toLowerCase(Locale.ROOT);

    MoveClass(final CooldownType defaultCooldownType) {
        this(defaultCooldownType, null);
    }

    MoveClass(final CooldownType defaultCooldownType, final String key) {
        friendlyName = Component.translatable("jcraft.movetype." + name().toLowerCase(Locale.ROOT));
        this.key = Component.keybind(key == null ? "key.jcraft." + name().toLowerCase(Locale.ROOT) : key);
        this.defaultCooldownType = defaultCooldownType;
    }

    public static MoveClass randomMoveType() {
        return MoveClass.values()[random.nextInt(MoveClass.values().length)];
    }
}
