package net.arna.jcraft.common.loot;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.arna.jcraft.common.enchantments.CinderellasKissEnchantment;
import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class JLootTableHelper {
    private static final Multimap<Identifier, Consumer<LootTable.Builder>> modifications = MultimapBuilder.hashKeys().linkedHashSetValues().build();

    public static void init() {
        registerModification(JLootTableHelper::addMaskPool,
                new Identifier("chests/abandoned_mineshaft"),
                new Identifier("chests/buried_treasure"),
                new Identifier("chests/end_city_treasure"),
                new Identifier("chests/pillager_outpost"),
                new Identifier("chests/simple_dungeon"),
                new Identifier("chests/spawn_bonus_chest"),
                new Identifier("chests/stronghold_library"),
                new Identifier("chests/woodland_mansion")
        );

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            for (Consumer<LootTable.Builder> modification : modifications.get(id)) modification.accept(tableBuilder);
        });
    }

    public static void registerModification(Consumer<LootTable.Builder> modifier, Identifier... lootTables) {
        for (Identifier lootTable : lootTables) modifications.put(lootTable, modifier);
    }

    private static void addMaskPool(LootTable.Builder builder) {
        builder.pool(LootPool.builder()
                .with(ItemEntry.builder(JObjectRegistry.CINDERELLA_MASK)
                        .weight(1) // 33% chance
                        .apply(new SetEnchantmentsLootFunction.Builder()
                                // Binomial distribution with n = 3, p = 0.4, plotter here:
                                // https://homepage.divms.uiowa.edu/~mbognar/applets/bin.html
                                // P(0) = 21.6%; P(1) = 43.2%; P(2) = 28.8%; P(3) = 6.4%
                                .enchantment(CinderellasKissEnchantment.INSTANCE, BinomialLootNumberProvider.create(3, 0.4f))))
                .with(ItemEntry.builder(Items.BOOK)
                        .weight(2) // 67% chance
                        .apply(new SetEnchantmentsLootFunction.Builder()
                                // Enchant with at least level 1
                                .enchantment(CinderellasKissEnchantment.INSTANCE, ConstantLootNumberProvider.create(1)))
                        .apply(new SetEnchantmentsLootFunction.Builder(true)
                                // Add up to 2 levels to the kiss enchantment
                                // n = 2, p = 0.25
                                // P(0) = 56.25%; P(1) = 37.5%; P(2) = 6.25%
                                .enchantment(CinderellasKissEnchantment.INSTANCE, BinomialLootNumberProvider.create(2, 0.25f))))
                .conditionally(RandomChanceLootCondition.builder(0.08f)));

        builder.pool(LootPool.builder()
                .with(ItemEntry.builder(JObjectRegistry.STONE_MASK))
                .conditionally(RandomChanceLootCondition.builder(0.04f)));
    }
}
