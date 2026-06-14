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

public class IntOption extends ConfigOption {
    @Getter
    private int value;
    @Getter
    private final int defaultValue;
    @Getter
    private Integer min, max;

    public IntOption(final ResourceLocation key, final ResourceLocation category, final int value) {
        super(key, category);
        this.value = this.defaultValue = value;
    }

    public IntOption(final ResourceLocation key, final ResourceLocation category, final int value, final int min) {
        super(key, category);
        this.value = this.defaultValue = value;
        this.min = min;
    }

    public IntOption(final ResourceLocation key, final ResourceLocation category, final int value, final int min, final int max) {
        super(key, category);
        this.value = this.defaultValue = value;
        this.min = min;
        this.max = max;
    }

    public void setValue(int value) {
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
        buf.writeVarInt(value);
    }

    @Override
    public void read(final FriendlyByteBuf buf) {
        value = buf.readVarInt();
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }

    @Override
    public void read(final JsonElement element) {
        value = element.getAsInt();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AbstractFieldBuilder<?, ?, ?> createField(ConfigBuilder builder, Component name, Runnable markDirty) {
        // If we have both a min and a max, we make a slider.
        if (getMin() != null && getMax() != null) {
            return builder.entryBuilder().startIntSlider(name, getValue(), getMin(), getMax())
                    .setDefaultValue(getDefaultValue())
                    .setSaveConsumer(value -> {
                        setValue(value);
                        markDirty.run();
                    });
        }

        return builder.entryBuilder().startIntField(name, getValue())
                .setDefaultValue(getDefaultValue())
                .setMin(getMin())
                .setMax(getMax())
                .setSaveConsumer(value1 -> {
                    setValue(value1);
                    markDirty.run();
                });
    }
}
