package net.arna.jcraft.api.serverconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StringOption extends ConfigOption {
    @Getter @Setter @NonNull
    private String value;
    @Getter
    private final String defaultValue;

    public StringOption(ResourceLocation key, ResourceLocation category, String defaultValue) {
        super(key, category);
        this.value = this.defaultValue = defaultValue;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(value);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.value = buf.readUtf();
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public void read(JsonElement element) {
        this.value = element.getAsString();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AbstractFieldBuilder<?, ?, ?> createField(ConfigBuilder builder, Component name, Runnable markDirty) {
        return builder.entryBuilder().startTextField(name, value)
                .setDefaultValue(defaultValue)
                .setSaveConsumer(newValue -> {
                    this.value = newValue;
                    markDirty.run();
                });
    }
}
