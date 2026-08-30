package net.arna.jcraft.common.command;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.arna.jcraft.api.IAttackerType;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.argumenttype.SpecArgumentType;
import net.arna.jcraft.common.argumenttype.StandArgumentType;
import net.arna.jcraft.common.command.permissions.JPerms;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JCraftChangesCommand {
    private static Map<ResourceLocation, List<Pair<String, List<Pair<Component, MapDifference<String, Object>>>>>> CHANGES;

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jcraft")
                .then(Commands.literal("changes")
                        .requires(JPerms.CHANGES.require())
                        .executes(ctx -> run(ctx, null))
                        .then(Commands.argument("stand", StandArgumentType.stand())
                                .requires(JPerms.CHANGES_STAND.require())
                                .executes(ctx -> run(ctx, ctx.getArgument("stand", StandType.class))))
                        .then(Commands.argument("spec", SpecArgumentType.spec())
                                .requires(JPerms.CHANGES_SPEC.require())
                                .executes(ctx -> run(ctx, ctx.getArgument("spec", SpecType.class))))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, IAttackerType type) {
        if (CHANGES == null) ctx.getSource().sendSuccess(() -> Component.translatable("jcraft.commands.changes.looking"), true);

        (CHANGES == null ? CompletableFuture.supplyAsync(JCraftChangesCommand::findChanges) : CompletableFuture.completedFuture(CHANGES))
                .thenAccept(changes -> {
                    CHANGES = changes;

                    if (type != null) {
                        sendChanges(ctx, type.getId(), changes.get(type.getId()));
                        return;
                    }

                    changes.forEach((type1, typeChanges) -> {
                        if (noChanges(typeChanges)) return;
                        sendChanges(ctx, type1, typeChanges);
                    });
                });

        return 1;
    }

    private static void sendChanges(final CommandContext<CommandSourceStack> ctx, final ResourceLocation type,
                                    List<Pair<String, List<Pair<Component, MapDifference<String, Object>>>>> changes) {
        ctx.getSource().sendSuccess(() -> Component.translatable("jcraft.commands.changes.type", type), true);

        if (noChanges(changes)) {
            ctx.getSource().sendSuccess(() -> Component.empty().append("    ")
                    .append(Component.translatable("jcraft.commands.changes.no_changes").withStyle(ChatFormatting.ITALIC)), true);
            return;
        }

        for (Pair<String, List<Pair<Component, MapDifference<String, Object>>>> moveSet : changes) {
            ctx.getSource().sendSuccess(() -> Component.empty().append("    ")
                    .append(Component.translatable("jcraft.commands.changes.move_set", moveSet.getFirst())), true);

            for (Pair<Component, MapDifference<String, Object>> moveChanges : moveSet.getSecond()) {
                MapDifference<String, Object> diff = moveChanges.getSecond();

                Map<String, MapDifference.ValueDifference<Object>> propsChanged = diff.entriesDiffering();
                Map<String, Object> propsAdded = diff.entriesOnlyOnRight();
                Map<String, Object> propsRemoved = diff.entriesOnlyOnLeft();

                propsChanged.forEach((prop, change) -> {
                    ctx.getSource().sendSuccess(() -> Component.empty().append("        ")
                            .append(Component.translatable("jcraft.commands.changes.property", moveChanges.getFirst(), prop)), true);

                    if (String.valueOf(change.leftValue()).length() > 50 || String.valueOf(change.rightValue()).length() > 50) {
                        ctx.getSource().sendSuccess(() -> Component.empty().append("            ")
                                .append(Component.translatable("jcraft.commands.changes.too_long").withStyle(ChatFormatting.ITALIC)), true);
                    } else {
                        ctx.getSource().sendSuccess(() -> Component.empty().append("            ")
                                .append(Component.translatable("jcraft.commands.changes.change",
                                        change.leftValue(), change.rightValue())), true);
                    }
                });

                propsAdded.forEach((prop, value) -> {
                    ctx.getSource().sendSuccess(() -> Component.empty().append("        ")
                            .append(Component.translatable("jcraft.commands.changes.property", moveChanges.getFirst(), prop)), true);
                    ctx.getSource().sendSuccess(() -> Component.empty().append("            ")
                            .append(Component.translatable("jcraft.commands.changes.addition",
                                            Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE))
                                    .withStyle(ChatFormatting.GREEN)), true);
                });

                propsRemoved.forEach((prop, value) -> {
                    ctx.getSource().sendSuccess(() -> Component.empty().append("        ")
                            .append(Component.translatable("jcraft.commands.changes.property", moveChanges.getFirst(), prop)), true);
                    ctx.getSource().sendSuccess(() -> Component.empty().append("            ")
                            .append(Component.translatable("jcraft.commands.changes.removal",
                                            Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE))
                                    .withStyle(ChatFormatting.RED)), true);
                });
            }
        }
    }

    private static boolean noChanges(final List<Pair<String, List<Pair<Component, MapDifference<String, Object>>>>> changes) {
        return changes.stream().allMatch(p -> p.getSecond().stream()
                .allMatch(p1 -> p1.getSecond().areEqual()));
    }

    private static @NotNull Map<ResourceLocation, @NotNull List<Pair<String, List<Pair<Component, MapDifference<String, Object>>>>>> findChanges() {
        return MoveSetManager.getMoveSets().entrySet().stream()
                .map(moveSets -> Pair.of(moveSets.getKey(), moveSets.getValue().values().stream()
                        .map(moveSet -> Pair.of(moveSet.getName(), findChanges(moveSet)))
                        .filter(p -> p.getSecond() != null)
                        .toList()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    /**
     * Find the changes between the original and modified move sets.
     * @param moveSet The move set to find changes in.
     * @return A list of move names paired with the changes made to them.
     */
    private static List<Pair<Component, MapDifference<String, Object>>> findChanges(MoveSet<?, ?> moveSet) {
        // Write the original and modified move sets to JSON
        DataResult<JsonElement> original = moveSet.write(JsonOps.INSTANCE);
        DataResult<JsonElement> modified = moveSet.writeModified(JsonOps.INSTANCE);

        // If either errored, return.
        if (original.result().isEmpty() || modified.result().isEmpty()) {
            return null;
        }

        // Extract the moves-array from JSON
        JsonArray originalMoves = original.result().get().getAsJsonObject().getAsJsonArray("moves");
        JsonArray modifiedMoves = modified.result().get().getAsJsonObject().getAsJsonArray("moves");

        // Flatten the maps
        List<Pair<Component, Map<String, Object>>> flattenedOriginal = flattenAndGetName(originalMoves);
        List<Pair<Component, Map<String, Object>>> flattenedModified = flattenAndGetName(modifiedMoves);

        // Find the differences
        //noinspection UnstableApiUsage
        return Streams.zip(flattenedOriginal.stream(), flattenedModified.stream(), Pair::of)
                .map(p -> Pair.of(p.getFirst().getFirst(), Maps.difference(p.getFirst().getSecond(), p.getSecond().getSecond())))
                .toList();
    }

    private static List<Pair<Component, Map<String, Object>>> flattenAndGetName(JsonArray moves) {
        return moves.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(obj -> {
                    // Extract the name from the object
                    JsonObject jsonName = Optional.ofNullable(obj.getAsJsonObject("move"))
                            .flatMap(obj1 -> Optional.ofNullable(obj1.getAsJsonObject("extras")))
                            .map(obj1 -> obj1.getAsJsonObject("name"))
                            .orElseGet(JsonObject::new);
                    DataResult<Pair<Component, JsonElement>> nameRes = ExtraCodecs.COMPONENT.decode(JsonOps.INSTANCE, jsonName);
                    Component name = nameRes.result().map(Pair::getFirst).orElse(Component.literal("ERROR"));

                    // Convert to map and flatten
                    return Pair.of(name, flatten(asMap(obj)));
                })
                .toList();
    }

    // Source: https://stackoverflow.com/a/50969020/10099540
    private static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet().stream()
                .flatMap(JCraftChangesCommand::flatten)
                .collect(LinkedHashMap::new, (m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {
        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            return ((Map<?, ?>) entry.getValue()).entrySet().stream()
                    .flatMap(e -> flatten(new AbstractMap.SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof final List<?> list) {
            return IntStream.range(0, list.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
                    .flatMap(JCraftChangesCommand::flatten);
        }

        return Stream.of(entry);
    }

    private static Object toObject(JsonElement element) {
        if (element.isJsonObject()) {
            return asMap(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            return asList(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            return asPrimitive(element.getAsJsonPrimitive());
        } else if (element.isJsonNull()) {
            return null;
        } else {
            return element;
        }
    }

    private static Map<String, Object> asMap(JsonObject obj) {
        return obj.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), toObject(entry.getValue())))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a, LinkedHashMap::new));
    }

    private static List<Object> asList(JsonArray arr) {
        return arr.asList().stream()
                .map(JCraftChangesCommand::toObject)
                .collect(Collectors.toList());
    }

    private static Object asPrimitive(JsonPrimitive prim) {
        if (prim.isBoolean()) {
            return prim.getAsBoolean();
        } else if (prim.isNumber()) {
            return prim.getAsNumber();
        } else {
            return prim.getAsString();
        }
    }

    // Clear changes on reload
    public static CompletableFuture<Void> onReload(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                   ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                                   ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        CHANGES = null;
        return CompletableFuture.<Void>supplyAsync(() -> null, backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAccept(v -> {});
    }
}
