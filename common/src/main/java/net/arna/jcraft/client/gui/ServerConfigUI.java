package net.arna.jcraft.client.gui;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
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
    private static final String SUBCATEGORY_FORMAT = "%s.serverconfig.subcategory.%s";
    private static final String ADDONS_CATEGORY = "jcraft.serverconfig.category.addons";

    public static void show(final boolean editable) {
        final ConfigBuilder builder = ConfigBuilder.create();
        final Set<ConfigOption> changedOptions = new HashSet<>();

        Multimap<ResourceLocation, ConfigOption> byCategory = ConfigOption.getOptions().values().stream()
                .collect(Multimaps.toMultimap(ConfigOption::getCategory, o -> o,
                        () -> MultimapBuilder.linkedHashKeys().linkedHashSetValues().build()));

        List<Map.Entry<ResourceLocation, Collection<ConfigOption>>> entries = new ArrayList<>(byCategory.asMap().entrySet());
        entries.sort((e1, e2) -> {
            // Equal
            if (e1.getKey().equals(e2.getKey())) {
                return 0;
            }

            boolean firstJCraft = JCraft.MOD_ID.equals(e1.getKey().getNamespace());
            boolean secondJCraft = JCraft.MOD_ID.equals(e2.getKey().getNamespace());

            // First is from JCraft, second is not. Prioritize JCraft.
            if (firstJCraft && !secondJCraft) {
                return -1;
            }

            // Second is from JCraft, first is not. Prioritize JCraft.
            if (!firstJCraft && secondJCraft) {
                return 1;
            }

            // Either both are from JCraft or neither are. No preference.
            return 0;
        });

        for (final Map.Entry<ResourceLocation, Collection<ConfigOption>> catOptions : entries) {
            final ResourceLocation categoryId = catOptions.getKey();
            final String categoryKey = String.format(CATEGORY_FORMAT, categoryId.getNamespace(), categoryId.getPath());
            SubCategoryBuilder subcat = null;

            // If this category is not from JCraft (so from an addon) and has fewer than 5 options,
            // we put it under a general 'Addons' tab to prevent clutter.
            // Categories from addons that add 5 or more options do get their own tab.
            if (!JCraft.MOD_ID.equals(categoryId.getNamespace()) && catOptions.getValue().size() < 5)
                subcat = builder.entryBuilder().startSubCategory(Component.translatable(categoryKey));

            final ConfigCategory category = builder.getOrCreateCategory(Component.translatable(
                    subcat == null ? categoryKey : ADDONS_CATEGORY));

            // Categorize options in this category by subcategory
            Multimap<ResourceLocation, ConfigOption> bySubcategory = catOptions.getValue().stream()
                    .collect(Multimaps.toMultimap(ConfigOption::getSubcategory, o -> o,
                            () -> MultimapBuilder.linkedHashKeys().linkedHashSetValues().build()));

            for (Map.Entry<ResourceLocation, Collection<ConfigOption>> subcatOptions : bySubcategory.asMap().entrySet()) {
                ResourceLocation subcatId = subcatOptions.getKey();
                SubCategoryBuilder subcat1 = null;

                // This may or may not be a subcategory. If the key is null, it's not.
                if (subcatOptions.getKey() != null) {
                    final String subcategoryKey = String.format(SUBCATEGORY_FORMAT, subcatId.getNamespace(), subcatId.getPath());
                    subcat1 = builder.entryBuilder().startSubCategory(Component.translatable(subcategoryKey));
                }

                for (ConfigOption option : subcatOptions.getValue()) {
                    AbstractConfigListEntry<?> entry = createEntryForOption(option, builder, changedOptions);
                    if (subcat1 == null) {
                        if (subcat == null) category.addEntry(entry);
                        else subcat.add(entry);
                    }
                    else subcat1.add(entry);
                }

                if (subcat1 == null) continue;

                if (subcat == null) category.addEntry(subcat1.build());
                else subcat.add(subcat1.build());
            }

            if (subcat != null) category.addEntry(subcat.build());
        }

        builder.setEditable(editable);
        builder.setSavingRunnable(() -> NetworkManager.sendToServer(ConfigUpdatePacket.ID,
                JServerConfig.writeOptions(new FriendlyByteBuf(Unpooled.buffer()), changedOptions)));
        Minecraft.getInstance().setScreen(builder.build());
    }

    private static AbstractConfigListEntry<?> createEntryForOption(final ConfigOption option, final ConfigBuilder builder,
                                                                   final Set<ConfigOption> changedOptions) {
        final ResourceLocation key = option.getKey();

        final String optionKey = String.format(OPTION_FORMAT, key.getNamespace(), key.getPath());
        final String tooltipKey = optionKey + ".tooltip";

        final Component name = Component.translatable(optionKey);

        final AbstractFieldBuilder<?, ?, ?> entry = option.createField(builder, name, () -> changedOptions.add(option));

        if (I18n.exists(tooltipKey))
            entry.setTooltip(Component.translatable(tooltipKey));

        return entry.build();
    }
}
