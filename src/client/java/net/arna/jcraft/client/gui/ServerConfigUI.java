package net.arna.jcraft.client.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.*;
import net.arna.jcraft.common.config.*;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class ServerConfigUI {
    public static void show(boolean editable) {
        ConfigBuilder builder = ConfigBuilder.create();
        Set<ConfigOption> changedOptions = new HashSet<>();

        for (ConfigOption option : ConfigOption.getImmutableOptions().values()) {
            ConfigCategory category = builder.getOrCreateCategory(Text.translatable("jcraft.serverconfig.category." + option.getCategory()));
            MutableText optionText = Text.translatable("jcraft.serverconfig.option." + option.getKey());

            AbstractConfigListEntry<?> entry = switch (option.getType()) {
                case INTEGER -> {
                    IntOption intOption = (IntOption) option;
                    if (intOption.getMin() != null && intOption.getMax() != null) {
                        IntSliderBuilder sliderBuilder = builder.entryBuilder().startIntSlider(optionText, intOption.getValue(),
                                intOption.getMin(), intOption.getMax())
                                .setDefaultValue(intOption.getDefaultValue())
                                .setSaveConsumer(value -> {
                                    intOption.setValue(value);
                                    changedOptions.add(intOption);
                                });

                        yield sliderBuilder.build();
                    } else {
                        IntFieldBuilder fieldBuilder = builder.entryBuilder().startIntField(optionText, intOption.getValue())
                                .setDefaultValue(intOption.getDefaultValue())
                                .setSaveConsumer(value -> {
                                    intOption.setValue(value);
                                    changedOptions.add(intOption);
                                });

                        if (intOption.getMin() != null) fieldBuilder.setMin(intOption.getMin());
                        else fieldBuilder.removeMin();

                        if (intOption.getMax() != null) fieldBuilder.setMax(intOption.getMax());
                        else fieldBuilder.removeMax();

                        yield fieldBuilder.build();
                    }
                }
                case FLOAT -> {
                    FloatOption floatOption = (FloatOption) option;
                    FloatFieldBuilder fieldBuilder = builder.entryBuilder().startFloatField(optionText, floatOption.getValue())
                            .setDefaultValue(floatOption.getDefaultValue())
                            .setSaveConsumer(value -> {
                                floatOption.setValue(value);
                                changedOptions.add(floatOption);
                            });

                    if (floatOption.getMin() != null) fieldBuilder.setMin(floatOption.getMin());
                    else fieldBuilder.removeMin();

                    if (floatOption.getMax() != null) fieldBuilder.setMax(floatOption.getMax());
                    else fieldBuilder.removeMax();

                    yield fieldBuilder.build();
                }
                case BOOLEAN -> {
                    BooleanOption booleanOption = (BooleanOption) option;
                    BooleanToggleBuilder toggleBuilder = builder.entryBuilder().startBooleanToggle(optionText, booleanOption.getValue())
                            .setDefaultValue(booleanOption.getDefaultValue())
                            .setSaveConsumer(value -> {
                                booleanOption.setValue(value);
                                changedOptions.add(booleanOption);
                            });
                    yield toggleBuilder.build();
                }
                case ENUM -> {
                    EnumOption<?> enumOption = (EnumOption<?>) option;
                    //noinspection unchecked // this is fine
                    EnumSelectorBuilder<Enum<?>> selectorBuilder = builder.entryBuilder().startEnumSelector(optionText,
                                    (Class<Enum<?>>) enumOption.getClazz(), enumOption.getValue())
                            .setDefaultValue(enumOption.getDefaultValue())
                            .setSaveConsumer(e -> {
                                enumOption.setValue(e.ordinal());
                                changedOptions.add(enumOption);
                            });

                    yield selectorBuilder.build();
                }
            };

            category.addEntry(entry);
        }

        builder.setEditable(editable);
        builder.setSavingRunnable(() -> ClientPlayNetworking.send(ConfigUpdatePacket.ID, ConfigOption.writeOptions(
                PacketByteBufs.create(), changedOptions)));
        MinecraftClient.getInstance().setScreen(builder.build());
    }
}
