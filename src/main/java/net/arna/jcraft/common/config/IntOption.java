package net.arna.jcraft.common.config;

import lombok.Getter;
import net.minecraft.network.PacketByteBuf;

public class IntOption extends ConfigOption {
    @Getter
    private int value;
    @Getter
    private final int defaultValue;
    @Getter
    private Integer min, max;

    public IntOption(String key, String category, int value) {
        super(Type.INTEGER, key, category);
        this.value = this.defaultValue = value;
    }

    public IntOption(String key, String category, int value, int min) {
        super(Type.INTEGER, key, category);
        this.value = this.defaultValue = value;
        this.min = min;
    }

    public IntOption(String key, String category, int value, int min, int max) {
        super(Type.INTEGER, key, category);
        this.value = this.defaultValue = value;
        this.min = min;
        this.max = max;
    }

    public void setValue(int value) {
        if (min != null && value < min) value = min;
        if (max != null && value > max) value = max;
        this.value = value;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(value);
    }

    @Override
    public void read(PacketByteBuf buf) {
        value = buf.readVarInt();
    }
}
