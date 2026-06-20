package net.arna.jcraft.client.gui;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.arna.jcraft.JCraft;
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

import java.util.*;

@Environment(EnvType.CLIENT)
public class ServerConfigUI {
    private static final String OPTION_FORMAT = "%s.serverconfig.option.%s";
    private static final String CATEGORY_FORMAT = "%s.serverconfig.category.%s";
    private static final String ADDONS_CATEGORY = "jcraft.serverconfig.category.addons";

    public static void show(final boolean editable) {
        final ConfigBuilder builder = ConfigBuilder.create();
        final Set<ConfigOption> changedOptions = new HashSet<>();

        Multimap<ResourceLocation, ConfigOption> byCategory = ConfigOption.getOptions().values().stream()
                .collect(Multimaps.toMultimap(ConfigOption::getCategory, o -> o,
                        () -> MultimapBuilder.linkedHashKeys().hashSetValues().build()));

        List<Map.Entry<ResourceLocation, Collection<ConfigOption>>> entries = new ArrayList<>(byCategory.asMap().entrySet());
        entries.sort((e1, e2) -> {
            // Equal
            if (e1.getKey().equals(e2.getKey())) {
                return 0;
            }

            // First is from JCraft, second is not. Prioritize JCraft.
            if (JCraft.MOD_ID.equals(e1.getKey().getNamespace()) && !JCraft.MOD_ID.equals(e2.getKey().getNamespace())) {
                return -1;
            }

            // Second is from JCraft, first is not. Prioritize JCraft.
            if (!JCraft.MOD_ID.equals(e1.getKey().getNamespace()) && JCraft.MOD_ID.equals(e2.getKey().getNamespace())) {
                return 1;
            }

            // Either both are from JCraft or neither are. No preference.
            return 0;
        });

        for (final Map.Entry<ResourceLocation, Collection<ConfigOption>> catOptions : entries) {
            final ResourceLocation categoryId = catOptions.getKey();
            final String categoryKey = String.format(CATEGORY_FORMAT, categoryId.getNamespace(), categoryId.getPath());
            SubCategoryBuilder subCat = null;

            // If this category is not from JCraft (so from an addon) and has fewer than 5 options,
            // we put it under a general 'Addons' tab to prevent clutter.
            // Categories from addons that add 5 or more options do get their own tab.
            if (!JCraft.MOD_ID.equals(categoryId.getNamespace()) && catOptions.getValue().size() < 5)
                subCat = builder.entryBuilder().startSubCategory(Component.translatable(categoryKey));

            final ConfigCategory category = builder.getOrCreateCategory(Component.translatable(
                    subCat == null ? categoryKey : ADDONS_CATEGORY));

            for (final ConfigOption option : catOptions.getValue()) {
                final ResourceLocation key = option.getKey();

                final String optionKey = String.format(OPTION_FORMAT, key.getNamespace(), key.getPath());
                final String tooltipKey = optionKey + ".tooltip";

                final Component name = Component.translatable(optionKey);

                final AbstractFieldBuilder<?, ?, ?> entry = option.createField(builder, name, () -> changedOptions.add(option));

                if (I18n.exists(tooltipKey))
                    entry.setTooltip(Component.translatable(tooltipKey));

                if (subCat == null)
                    category.addEntry(entry.build());
                else subCat.add(entry.build());
            }

            if (subCat != null) {
                category.addEntry(subCat.build());
            }
        }

        builder.setEditable(editable);
        builder.setSavingRunnable(() -> NetworkManager.sendToServer(ConfigUpdatePacket.ID,
                JServerConfig.writeOptions(new FriendlyByteBuf(Unpooled.buffer()), changedOptions)));
        Minecraft.getInstance().setScreen(builder.build());
    }
}
