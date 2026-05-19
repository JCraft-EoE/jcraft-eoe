package net.arna.jcraft.common.villagertrades;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.*;

public class VillagerTradesModifier {
    public static void init() {
        Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> trades = new HashMap<>(VillagerTrades.TRADES);

        // TODO turn into a proper system
        Int2ObjectMap<VillagerTrades.ItemListing[]> cartographer = new Int2ObjectOpenHashMap<>(trades.get(VillagerProfession.CARTOGRAPHER));
        VillagerTrades.ItemListing[] oldItemListings = cartographer.getOrDefault(4, new VillagerTrades.ItemListing[0]);
        List<VillagerTrades.ItemListing> newItemListings = new ArrayList<>(Arrays.asList(oldItemListings));

        newItemListings.add(new VillagerTrades.TreasureMapForEmeralds(14, JTagRegistry.ON_MONASTERY_MAPS,
                "filled_map.jcraft.monastery", MapDecoration.Type.RED_X, 12, 20));

        cartographer.put(4, newItemListings.toArray(new VillagerTrades.ItemListing[0]));
        trades.put(VillagerProfession.CARTOGRAPHER, cartographer);
        VillagerTrades.TRADES = trades;
    }
}
