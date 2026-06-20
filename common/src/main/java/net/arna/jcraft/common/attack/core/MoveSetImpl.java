package net.arna.jcraft.common.attack.core;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.IAttackerType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.StateContainerHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class MoveSetImpl<A extends IAttacker<? extends A, S>, S extends Enum<S>> implements MoveSet<A, S> {
    private final RegistrySupplier<? extends IAttackerType> type;
    @Getter
    private final String name;
    private final Consumer<MoveMap<A, S>> register;
    @Getter
    private final Class<A> attackerClass;
    @Getter
    private final Class<S> stateClass;
    @Getter
    private final Codec<MoveMap<A, S>> codec;
    private final Codec<MoveMap.Entry<A, S>> entryCodec;
    private final Set<ReloadListener<A, S>> listeners = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    @Getter
    private final MoveMap<A, S> moveMap = new MoveMapImpl<>();
    @Getter
    private boolean initialized = false;

    public MoveSetImpl(@NonNull final RegistrySupplier<? extends IAttackerType> type, @NonNull final String name,
                       @NonNull final Consumer<MoveMap<A, S>> register, final Class<A> attackerClass,
                       @NonNull final Class<S> stateClass) {
        this.type = type;
        this.name = name;
        this.register = register;
        this.attackerClass = attackerClass;
        this.stateClass = stateClass;
        codec = MoveMap.codecFor(stateClass);
        entryCodec = MoveMap.Entry.codecFor(stateClass);

        if (attackerClass == null)
            JCraft.LOGGER.warn("Move set '{}' for attacker {} did not specify an attacker class. " +
                            "This is deprecated behaviour.", name, type.getId());
    }

    @Override
    public IAttackerType getType() {
        return type.get();
    }

    @Override
    public <T> DataResult<Pair<MoveMap<A, S>, T>> load(final DynamicOps<T> ops, T t, final Executor gameExecutor) {
        final DataResult<Pair<MoveMap<A, S>, T>> res = codec.decode(ops, t);
        if (res.result().isEmpty()) {
            return res;
        }

        onLoad(res.result().get().getFirst(), gameExecutor);
        return res;
    }

    @Override
    public <T> MoveMap<A, S> load(final DynamicOps<? super T> ops, final Collection<Pair<ResourceLocation, T>> ts, final Executor gameExecutor) {
        final List<MoveMap.Entry<A, S>> entries = ts.stream()
                // Decode each entry
                .map(p -> p.mapSecond(t -> entryCodec.decode(ops, t).map(Pair::getFirst)))
                // Extract results and log errors
                .map(p -> {
                    Optional<MoveMap.Entry<A, S>> result = p.getSecond().result();
                    if (result.isEmpty()) {
                        JCraft.LOGGER.error("Failed to decode move set entry {}: {}", p.getFirst(),
                                p.getSecond().error().map(DataResult.PartialResult::message).orElse("Unknown error"));
                        return null;
                    }

                    verifyCompatibility(p.getFirst(), result.get());
                    return result.get();
                })
                // Filter out nulls (failed decodes)
                .filter(Objects::nonNull)
                .toList();

        final MoveMap<A, S> moveMap = new MoveMapImpl<>(entries);
        onLoad(moveMap, gameExecutor);
        return moveMap;
    }

    @SuppressWarnings("unchecked")
    private void onLoad(final MoveMap<A, S> moveMap, final @Nullable Executor gameExecutor) {
        moveMap.asMovesList().stream()
                .filter(m -> m instanceof StateContainerHolder<?>)
                .map(m -> (StateContainerHolder<S>) m)
                .forEach(holder -> holder.configureStateContainers(stateClass));
        this.moveMap.copyFrom(moveMap);

        initialized = true;
        if (gameExecutor != null) gameExecutor.execute(() -> listeners.forEach(listener ->
                listener.onMoveSetReload(this)));
    }

    @Override
    public MoveMap<A, S> save() {
        final MoveMap<A, S> moveMap = new MoveMapImpl<>();
        register.accept(moveMap);
        return moveMap;
    }

    @Override
    public <T> DataResult<T> write(final DynamicOps<T> ops) {
        final MoveMap<A, S> moveMap = save();
        return codec.encodeStart(ops, moveMap);
    }

    @Override
    public <T> DataResult<T> writeModified(final DynamicOps<T> ops) {
        return codec.encodeStart(ops, moveMap);
    }

    @Override
    public void registerListener(final ReloadListener<A, S> listener) {
        if (!listeners.add(listener)) return;

        if (initialized) {
            listener.onMoveSetReload(this);
        }
    }

    private void verifyCompatibility(final @NonNull ResourceLocation moveId, final MoveMap.Entry<A, S> entry) {
        Class<?> moveAttackerClass = entry.getMove().getAttackerClass();
        if (moveAttackerClass == null) return; // Unknown, assume it supports all attackers.

        boolean compatible = moveAttackerClass.isAssignableFrom(attackerClass);
        if (!compatible) {
            throw new IllegalStateException("Move " + moveId + " is incompatible with attacker " + type.getId() + ".");
        }
    }
}
