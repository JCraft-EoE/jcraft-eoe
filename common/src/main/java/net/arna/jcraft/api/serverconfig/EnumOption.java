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

import java.util.Arrays;

@Getter
public class EnumOption<E extends Enum<?>> extends ConfigOption {
    private final Class<E> clazz;
    private E value;
    private final E defaultValue;

    public EnumOption(final ResourceLocation key, final ResourceLocation category, final Class<E> clazz, final E value) {
        super(key, category);
        this.clazz = clazz;
        this.value = this.defaultValue = value;
    }

    public void setValue(final int ordinal) {
        setValue(clazz.getEnumConstants()[ordinal]);
    }

    public void setValue(final E value) {
        this.value = value;
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeVarInt(value.ordinal());
    }

    @Override
    public void read(final FriendlyByteBuf buf) {
        setValue(buf.readVarInt());
    }

    @Override
    public JsonElement write() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void read(final JsonElement element) {
        String name = element.getAsString();
        value = Arrays.stream(clazz.getEnumConstants())
                .filter(e -> e.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AbstractFieldBuilder<?, ?, ?> createField(ConfigBuilder builder, Component name, Runnable markDirty) {
        return builder.entryBuilder().startEnumSelector(name, getClazz(), getValue())
                .setDefaultValue(getDefaultValue())
                .setSaveConsumer(e -> {
                    setValue(e.ordinal());
                    markDirty.run();
                });
    }
}
