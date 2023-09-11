package net.arna.jcraft.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.minecraft.network.PacketByteBuf;

import java.util.Arrays;

@Getter
public class EnumOption<E extends Enum<?>> extends ConfigOption {
    private final Class<E> clazz;
    private E value;
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

    @Override
    public JsonElement write() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void read(JsonElement element) {
        String name = element.getAsString();
        value = Arrays.stream(clazz.getEnumConstants())
                .filter(e -> e.name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
