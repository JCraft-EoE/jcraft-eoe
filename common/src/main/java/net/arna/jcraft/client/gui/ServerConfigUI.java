package net.arna.jcraft.client.gui;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractRangeFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.FloatFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import net.arna.jcraft.common.config.*;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashSet;
import java.util.Set;

public class ServerConfigUI {
    public static void show(final boolean editable) {
        final ConfigBuilder builder = ConfigBuilder.create();
        final Set<ConfigOption> changedOptions = new HashSet<>();

        for (final ConfigOption option : ConfigOption.getImmutableOptions().values()) {
            final String tooltipKey = "jcraft.serverconfig.option." + option.getKey() + ".tooltip";
            final String categoryKey = "jcraft.serverconfig.category." + option.getCategory();
            final String optionKey = "jcraft.serverconfig.option." + option.getKey();

            final ConfigCategory category = builder.getOrCreateCategory(Component.translatable(categoryKey));
            final MutableComponent optionText = Component.translatable(optionKey);

            final AbstractFieldBuilder<?, ?, ?> entry = switch (option.getType()) {
                case INTEGER -> {
                    final IntOption intOption = (IntOption) option;
                    if (intOption.getMin() != null && intOption.getMax() != null) {
                        yield builder.entryBuilder().startIntSlider(optionText, intOption.getValue(),
                                        intOption.getMin(), intOption.getMax())
                                .setDefaultValue(intOption.getDefaultValue())
                                .setSaveConsumer(value -> {
                                    intOption.setValue(value);
                                    changedOptions.add(intOption);
                                });
                    } else {
                        final IntFieldBuilder fieldBuilder = builder.entryBuilder().startIntField(optionText, intOption.getValue())
                                .setDefaultValue(intOption.getDefaultValue())
                                .setSaveConsumer(value -> {
                                    intOption.setValue(value);
                                    changedOptions.add(intOption);
                                });

                        setMinMax(fieldBuilder, intOption.getMin(), intOption.getMax());

                        yield fieldBuilder;
                    }
                }
                case FLOAT -> {
                    final FloatOption floatOption = (FloatOption) option;
                    final FloatFieldBuilder fieldBuilder = builder.entryBuilder().startFloatField(optionText, floatOption.getValue())
                            .setDefaultValue(floatOption.getDefaultValue())
                            .setSaveConsumer(value -> {
                                floatOption.setValue(value);
                                changedOptions.add(floatOption);
                            });

                    setMinMax(fieldBuilder, floatOption.getMin(), floatOption.getMax());

                    yield fieldBuilder;
                }
                case BOOLEAN -> {
                    final BooleanOption booleanOption = (BooleanOption) option;
                    yield builder.entryBuilder().startBooleanToggle(optionText, booleanOption.getValue())
                            .setDefaultValue(booleanOption.getDefaultValue())
                            .setSaveConsumer(value -> {
                                booleanOption.setValue(value);
                                changedOptions.add(booleanOption);
                            });
                }
                case ENUM -> {
                    final EnumOption<?> enumOption = (EnumOption<?>) option;
                    //noinspection unchecked // this is fine
                    yield builder.entryBuilder().startEnumSelector(optionText,
                                    (Class<Enum<?>>) enumOption.getClazz(), enumOption.getValue())
                            .setDefaultValue(enumOption.getDefaultValue())
                            .setSaveConsumer(e -> {
                                enumOption.setValue(e.ordinal());
                                changedOptions.add(enumOption);
                            });
                }
            };

            if (I18n.exists(tooltipKey))
                entry.setTooltip(Component.translatable(tooltipKey));

            category.addEntry(entry.build());
        }

        builder.setEditable(editable);
        builder.setSavingRunnable(() -> NetworkManager.sendToServer(ConfigUpdatePacket.ID, ConfigOption.writeOptions(
                new FriendlyByteBuf(Unpooled.buffer()), changedOptions)));
        Minecraft.getInstance().setScreen(builder.build());
    }

    private static <T extends Number> void setMinMax(AbstractRangeFieldBuilder<T, ?, ?> fieldBuilder, T min, T max) {
        if (min != null) {
            fieldBuilder.setMin(min);
        } else {
            fieldBuilder.removeMin();
        }

        if (max != null) {
            fieldBuilder.setMax(max);
        } else {
            fieldBuilder.removeMax();
        }
    }
}
