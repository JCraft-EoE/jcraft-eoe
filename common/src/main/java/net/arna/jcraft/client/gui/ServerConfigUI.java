package net.arna.jcraft.client.gui;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.arna.jcraft.api.serverconfig.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ServerConfigUI {
    private static final String OPTION_FORMAT = "%s.serverconfig.option.%s";
    private static final String CATEGORY_FORMAT = "%s.serverconfig.category.%s";

    public static void show(final boolean editable) {
        final ConfigBuilder builder = ConfigBuilder.create();
        final Set<ConfigOption> changedOptions = new HashSet<>();

        for (final ConfigOption option : ConfigOption.getOptions().values()) {
            final ResourceLocation key = option.getKey();
            final ResourceLocation categoryId = option.getCategory();

            final String categoryKey = String.format(CATEGORY_FORMAT, categoryId.getNamespace(), categoryId.getPath());
            final String optionKey = String.format(OPTION_FORMAT, key.getNamespace(), key.getPath());
            final String tooltipKey = optionKey + ".tooltip";

            final ConfigCategory category = builder.getOrCreateCategory(Component.translatable(categoryKey));
            final Component name = Component.translatable(optionKey);

            final AbstractFieldBuilder<?, ?, ?> entry = option.createField(builder, name, () -> changedOptions.add(option));

            if (I18n.exists(tooltipKey))
                entry.setTooltip(Component.translatable(tooltipKey));

            category.addEntry(entry.build());
        }

        builder.setEditable(editable);
        builder.setSavingRunnable(() -> NetworkManager.sendToServer(ConfigUpdatePacket.ID,
                JServerConfig.writeOptions(new FriendlyByteBuf(Unpooled.buffer()), changedOptions)));
        Minecraft.getInstance().setScreen(builder.build());
    }
}
