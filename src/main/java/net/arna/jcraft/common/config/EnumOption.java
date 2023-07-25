package net.arna.jcraft.common.config;

import lombok.Getter;
import net.minecraft.network.PacketByteBuf;

public class EnumOption<E extends Enum<?>> extends ConfigOption {
    @Getter
    private final Class<E> clazz;
    @Getter
    private E value;
    @Getter
    private final E defaultValue;

    public EnumOption(String key, String category, Class<E> clazz, E value) {
        super(Type.ENUM, key, category);
        this.clazz = clazz;
        this.value = this.defaultValue = value;
    }

    public void setValue(int ordinal) {
        setValue(clazz.getEnumConstants()[ordinal]);
    }

    public void setValue(E value) {
        this.value = value;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(value.ordinal());
    }

    @Override
    public void read(PacketByteBuf buf) {
        setValue(buf.readVarInt());
    }
}
