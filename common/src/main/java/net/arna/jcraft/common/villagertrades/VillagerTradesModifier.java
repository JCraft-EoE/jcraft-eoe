package net.arna.jcraft.common.villagertrades;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.*;

public class VillagerTradesModifier {
    private static final Multimap<VillagerProfession, JVillagerTrade> newTrades =
            ImmutableMultimap.<VillagerProfession, JVillagerTrade>builder()
                    .putAll(VillagerProfession.CARTOGRAPHER,
                            new JVillagerTrade(5, new VillagerTrades.TreasureMapForEmeralds(14,
                                    JTagRegistry.ON_MONASTERY_MAPS, "filled_map.jcraft.monastery",
                                    MapDecoration.Type.RED_X, 12, 20)),
                            new JVillagerTrade(5, new VillagerTrades.TreasureMapForEmeralds(14,
                                    JTagRegistry.ON_VAMPIRE_LAIR_MAPS, "filled_map.jcraft.vampire_lair",
                                    MapDecoration.Type.RED_X, 12, 20)),
                            new JVillagerTrade(4, 4, new VillagerTrades.TreasureMapForEmeralds(14,
                                    JTagRegistry.ON_CINDERELLA_MAPS, "filled_map.jcraft.cinderella",
                                    MapDecoration.Type.RED_X, 12, 20)),
                            new JVillagerTrade(3, new VillagerTrades.TreasureMapForEmeralds(14,
                                    JTagRegistry.ON_METEORITE_MAPS, "filled_map.jcraft.meteorite",
                                    MapDecoration.Type.RED_X, 12, 20)))
                    .build();

    public static void init() {
        injectJCraftTrades();
    }

    private static void injectJCraftTrades() {
        // TRADES field has been made mutable using an access widener.
        Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> trades = new HashMap<>(VillagerTrades.TRADES);

        newTrades.asMap().forEach((profession, profTrades) -> {
            Int2ObjectMap<VillagerTrades.ItemListing[]> masteryListings = trades.getOrDefault(profession, new Int2ObjectOpenHashMap<>());

            profTrades.forEach(trade -> {
                VillagerTrades.ItemListing[] listings = masteryListings.getOrDefault(trade.masteryLevel(), new VillagerTrades.ItemListing[0]);
                List<VillagerTrades.ItemListing> listingsList = new ArrayList<>(Arrays.asList(listings));

                for (int i = 0; i < trade.weight(); i++) {
                    listingsList.add(trade.listing());
                }

                masteryListings.put(trade.masteryLevel(), listingsList.toArray(VillagerTrades.ItemListing[]::new));
            });

            trades.put(profession, masteryListings);
        });

        VillagerTrades.TRADES = trades;
    }

    private record JVillagerTrade(int masteryLevel, int weight, VillagerTrades.ItemListing listing) {
        public JVillagerTrade(int masteryLevel, VillagerTrades.ItemListing listing) {
            this(masteryLevel, 1, listing);
        }
    }
}
