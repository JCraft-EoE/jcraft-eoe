package net.arna.jcraft.api.serverconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FloatOption extends ConfigOption {
    @Getter
    private float value;
    @Getter
    private final float defaultValue;
    @Getter
    private Float min, max;

    public FloatOption(final ResourceLocation key, final ResourceLocation category, final float value) {
        this(key, category, null, value);
    }

    public FloatOption(final ResourceLocation key, final ResourceLocation category, final ResourceLocation subcategory, final float value) {
        super(key, category, null);
        this.value = this.defaultValue = value;
    }

    public FloatOption(final ResourceLocation key, final ResourceLocation category, final float value, final float min) {
        this(key, category, null, value, min);
    }

    public FloatOption(final ResourceLocation key, final ResourceLocation category, final ResourceLocation subcategory,
                       final float value, final float min) {
        super(key, category, subcategory);
        this.value = this.defaultValue = value;
        this.min = min;
    }

    public FloatOption(final ResourceLocation key, final ResourceLocation category, final float value, final float min,
                       final float max) {
        this(key, category, null, value, min, max);
    }

    public FloatOption(final ResourceLocation key, final ResourceLocation category, final ResourceLocation subcategory,
                       final float value, final float min, final float max) {
        super(key, category, subcategory);
        this.value = this.defaultValue = value;
        this.min = min;
        this.max = max;
    }

    public void setValue(float value) {
        if (min != null && value < min) {
            value = min;
        }
        if (max != null && value > max) {
            value = max;
        }
        this.value = value;
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeFloat(value);
    }

    @Override
    public void read(final FriendlyByteBuf buf) {
        value = buf.readFloat();
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }

    @Override
    public void read(final JsonElement element) {
        value = element.getAsFloat();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AbstractFieldBuilder<?, ?, ?> createField(ConfigBuilder builder, Component name, Runnable markDirty) {
        return builder.entryBuilder().startFloatField(name, getValue())
                .setDefaultValue(getDefaultValue())
                .setMin(getMin())
                .setMax(getMax())
                .setSaveConsumer(value1 -> {
                    setValue(value1);
                    markDirty.run();
                });
    }
}
