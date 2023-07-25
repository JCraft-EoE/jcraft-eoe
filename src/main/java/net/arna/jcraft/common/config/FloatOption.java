package net.arna.jcraft.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.minecraft.network.PacketByteBuf;

public class FloatOption extends ConfigOption {
    @Getter
    private float value;
    @Getter
    private final float defaultValue;
    @Getter
    private Float min, max;

    public FloatOption(String key, String category, float value) {
        super(Type.FLOAT, key, category);
        this.value = this.defaultValue = value;
    }

    public FloatOption(String key, String category, float value, float min) {
        super(Type.FLOAT, key, category);
        this.value = this.defaultValue = value;
        this.min = min;
    }

    public FloatOption(String key, String category, float value, float min, float max) {
        super(Type.FLOAT, key, category);
        this.value = this.defaultValue = value;
        this.min = min;
        this.max = max;
    }

    public void setValue(float value) {
        if (min != null && value < min) value = min;
        if (max != null && value > max) value = max;
        this.value = value;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(value);
    }

    @Override
    public void read(PacketByteBuf buf) {
        value = buf.readFloat();
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }

    @Override
    public void read(JsonElement element) {
        value = element.getAsFloat();
    }
}
