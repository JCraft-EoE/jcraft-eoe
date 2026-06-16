package net.arna.jcraft.api.serverconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Setter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BooleanOption extends ConfigOption {
    @Setter
    private boolean value;
    private final boolean defaultValue;

    public BooleanOption(final ResourceLocation key, final ResourceLocation category, final boolean value) {
        super(key, category);
        this.value = this.defaultValue = value;
    }

    // Lombok names it isValue which makes no sense here.
    public boolean getValue() {
        return value;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeBoolean(value);
    }

    @Override
    public void read(final FriendlyByteBuf buf) {
        value = buf.readBoolean();
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }

    @Override
    public void read(final JsonElement element) {
        value = element.getAsBoolean();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AbstractFieldBuilder<?, ?, ?> createField(ConfigBuilder builder, Component name, Runnable markDirty) {
        return builder.entryBuilder().startBooleanToggle(name, getValue())
                .setDefaultValue(getDefaultValue())
                .setSaveConsumer(value -> {
                    setValue(value);
                    markDirty.run();
                });
    }
}
