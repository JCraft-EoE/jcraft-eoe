package net.arna.jcraft.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.saveddata.ExclusiveStandsData;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EvolutionItemHandler {
    private static final Multimap<Item, Evolution> evolutions = MultimapBuilder.hashKeys().arrayListValues().build();

    static {
        InteractionEvent.RIGHT_CLICK_ITEM.register(EvolutionItemHandler::onItemUse);
    }

    public static void init() {}

    public static List<Evolution> getEvolutions() {
        return ImmutableList.copyOf(evolutions.values());
    }


    private static CompoundEventResult<ItemStack> onItemUse(Player player, InteractionHand hand) {
        if (player.level().isClientSide()) return CompoundEventResult.pass();

        ItemStack handStack = player.getItemInHand(hand);
        Item item = handStack.getItem();
        final ExclusiveStandsData exclusiveStands = JCraft.getExclusiveStandsData();

        if (!evolutions.containsKey(item)) {
            return CompoundEventResult.pass();
        }

        Collection<Evolution> evolutions = EvolutionItemHandler.evolutions.get(item);
        CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(player);
        StandType current = standData.getType();

        // If there's a more specific evolution that matches, use that.
        // Otherwise, if there's one with a null input, use that.
        Evolution fallback = evolutions.stream()
                .filter(e -> e.predicate().matches(handStack) && e.stand() == null)
                .findFirst()
                .orElse(null);

        Evolution evolution = fallback;
        if (current != null) evolution = evolutions.stream()
                .filter(e -> e.predicate().matches(handStack) && e.stand() == current)
                .findFirst()
                .orElse(fallback);

        if (evolution == null) return CompoundEventResult.pass();

        // Check if target stand is already in use.
        if (exclusiveStands.isStandUsed(evolution.target())) {
            return CompoundEventResult.pass();
        }

        if (!player.isCreative()) handStack.shrink(1);

        standData.setTypeAndSkin(evolution.target(), standData.getSkin(), false);
        player.awardStat(JStatRegistry.STAND_EVOLVED.get());

        // Re-summon users stand
        StandEntity<?, ?> stand = standData.getStand();
        if (stand != null) {
            stand.desummon();
        }
        JCraft.summon(player.level(), player);

        return CompoundEventResult.interruptTrue(handStack);
    }

    public static CompletableFuture<Void> onReload(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                   ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                                   ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> readEvolutions(resourceManager), backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAccept(evs -> evs.forEach(e -> evolutions.put(e.item(), e)));
    }

    private static Collection<Evolution> readEvolutions(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> evolutionFiles = resourceManager.listResources("evolutions",
                rl -> rl.getPath().endsWith(".json"));

        Gson gson = new Gson();
        List<Evolution> evolutions = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, Resource> entry : evolutionFiles.entrySet()) {
            JsonObject obj;
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                obj = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                JCraft.LOGGER.error("Failed to read evolutions from {}", entry.getKey(), e);
                continue;
            }

            Either<Evolution, DataResult.PartialResult<Evolution>> res = Evolution.CODEC.parse(JsonOps.INSTANCE, obj).get();
            res.ifLeft(evolutions::add);
            res.ifRight(err -> JCraft.LOGGER.error("Failed to parse evolutions from {}: {}", entry.getKey(), err.message()));
        }

        return evolutions;
    }

    public record Evolution(Item item, ItemPredicate predicate, @Nullable StandType stand, StandType target) {
        public static final Codec<Evolution> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Evolution::item),
                JCodecUtils.ITEM_PREDICATE_CODEC.optionalFieldOf("predicate", ItemPredicate.ANY).forGetter(Evolution::predicate),
                JRegistries.STAND_TYPE_CODEC.optionalFieldOf("stand").forGetter(e -> Optional.ofNullable(e.stand())),
                JRegistries.STAND_TYPE_CODEC.fieldOf("target").forGetter(Evolution::target)
        ).apply(instance, Evolution::new));

        public Evolution(Item item, @Nullable StandType stand, StandType target) {
            this(item, ItemPredicate.ANY, stand, target);
        }

        private Evolution(Item item, ItemPredicate predicate, Optional<StandType> stand, StandType target) {
            this(item, predicate, stand.orElse(null), target);
        }
    }
}
