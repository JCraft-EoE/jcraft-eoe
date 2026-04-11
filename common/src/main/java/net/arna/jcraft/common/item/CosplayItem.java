package net.arna.jcraft.common.item;

import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class CosplayItem<T extends ArmorItem> implements Iterable<RegistrySupplier<T>> {

    // all these LinkedHashMaps are justified, trust me - Ayutac
    public static final Map<ArmorMaterial, String> SUFFIXES = new LinkedHashMap<>();
    public static final Map<ArmorMaterial, String> VAMPIRE_SUFFIXES = new LinkedHashMap<>();
    public static final Map<RegistrySupplier<? extends ArmorItem>, CosplayItem<?>> LOOKUP = new LinkedHashMap<>();

    static {
        SUFFIXES.put(ArmorMaterials.LEATHER, "_leather");
        SUFFIXES.put(ArmorMaterials.GOLD, "_gold");
        SUFFIXES.put(ArmorMaterials.CHAIN, "_chainmail");
        SUFFIXES.put(ArmorMaterials.IRON, "_iron");
        SUFFIXES.put(ArmorMaterials.DIAMOND, "_diamond");
        SUFFIXES.put(ArmorMaterials.TURTLE, "_turtle");
        SUFFIXES.put(ArmorMaterials.NETHERITE, "");
        VAMPIRE_SUFFIXES.put(ArmorMaterials.LEATHER, "_leather");
        VAMPIRE_SUFFIXES.put(ArmorMaterials.GOLD, "_gold");
        VAMPIRE_SUFFIXES.put(ArmorMaterials.CHAIN, "_chainmail");
        VAMPIRE_SUFFIXES.put(ArmorMaterials.IRON, "");
    }

    protected final String modId;
    @Getter
    protected final String name;
    @Getter
    protected final TagKey<Item> tag;
    @Getter
    protected final ArmorItem.Type slot;
    @Getter
    protected final boolean vampireProtection;
    protected final CosplayItemConstructor<T> ctor;

    protected final Map<ArmorMaterial, RegistrySupplier<T>> items = new LinkedHashMap<>();

    public CosplayItem(final @NonNull String modId, final @NonNull String name, final @NonNull ArmorItem.Type slot, final boolean vampireProtection, final @NonNull CosplayItemConstructor<T> ctor) {
        this.modId = modId;
        this.name = name;
        tag = TagKey.create(Registries.ITEM, new ResourceLocation(modId, name));
        this.slot = slot;
        this.vampireProtection = vampireProtection;
        this.ctor = ctor;
    }

    public CosplayItem(final @NonNull String modId, final @NonNull String name, final @NonNull ArmorItem.Type slot, final @NonNull CosplayItemConstructor<T> ctor) {
        this(modId, name, slot, false, ctor);
    }

    public CosplayItem<T> register(final @NonNull CosplayItemRegistrator<T> registrator) {
        for (final var entry : (vampireProtection ? VAMPIRE_SUFFIXES.entrySet() : SUFFIXES.entrySet())) {
            // only register turtle for helmets
            if (entry.getKey() == ArmorMaterials.TURTLE && slot != ArmorItem.Type.HELMET) {
                continue;
            }
            final Item.Properties properties;
            if (entry.getKey() == ArmorMaterials.NETHERITE) {
                properties = new Item.Properties().fireResistant();
            }
            else {
                properties = new Item.Properties();
            }
            final RegistrySupplier<T> registrySupplier = registrator.register(name + entry.getValue(), () -> ctor.create(entry.getKey(), slot, properties));
            items.put(entry.getKey(), registrySupplier);
            LOOKUP.put(registrySupplier, this);
        }
        return this;
    }

    public @Nullable RegistrySupplier<T> get(final @NonNull ArmorMaterial material) {
        return items.get(material);
    }

    public Collection<RegistrySupplier<T>> getAll() {
        return items.values();
    }

    @Override
    public @NotNull Iterator<RegistrySupplier<T>> iterator() {
        return items.values().iterator();
    }

    public static @NonNull List<CosplayItem<?>> all() {
        return LOOKUP.values().stream().distinct().toList();
    }

    public static @Nullable CosplayItem<?> find(final @Nullable Item item) {
        if (!(item instanceof ArmorItem)) {
            return null;
        }
        for (final var lookupEntry : LOOKUP.entrySet()) {
            if (lookupEntry.getKey().get() == item) {
                return lookupEntry.getValue();
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface CosplayItemConstructor<S extends ArmorItem> {
        S create(final @NonNull ArmorMaterial materialIn, final @NonNull ArmorItem.Type slot, final @NonNull Item.Properties builder);
    }

    @FunctionalInterface
    public interface CosplayItemRegistrator<S extends ArmorItem> {
        RegistrySupplier<S> register(String id, Supplier<? extends S> supplier);
    }
}
