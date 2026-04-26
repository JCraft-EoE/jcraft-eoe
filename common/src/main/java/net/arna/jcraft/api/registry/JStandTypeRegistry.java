package net.arna.jcraft.api.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.function.Supplier;

public interface JStandTypeRegistry {
    DeferredRegister<StandType> STAND_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, JRegistries.STAND_TYPE_REGISTRY_KEY);

    /**
     * The NONE stand type data, used when the mob/player has no stand.
     * Different from a {@code null} stand type in that mobs with this type
     * will not get a stand at all, while mobs with a {@code null} stand type
     * may yet get a stand assigned.
     * <p>
     * For players, this and {@code null} are equivalent.
     */
    RegistrySupplier<StandType> NONE = register("none", () -> null);
    RegistrySupplier<StandType> STAR_PLATINUM = register("star_platinum", JEntityTypeRegistry.STAR_PLATINUM);
    RegistrySupplier<StandType> THE_WORLD = register("the_world", JEntityTypeRegistry.THE_WORLD);
    RegistrySupplier<StandType> KING_CRIMSON = register("king_crimson", JEntityTypeRegistry.KING_CRIMSON);
    RegistrySupplier<StandType> D4C = register("d4c", JEntityTypeRegistry.D4C);
    RegistrySupplier<StandType> CREAM = register("cream", JEntityTypeRegistry.CREAM);
    RegistrySupplier<StandType> KILLER_QUEEN = register("killer_queen", JEntityTypeRegistry.KILLER_QUEEN);
    RegistrySupplier<StandType> WHITE_SNAKE = register("white_snake", JEntityTypeRegistry.WHITE_SNAKE);
    RegistrySupplier<StandType> SILVER_CHARIOT = register("silver_chariot", JEntityTypeRegistry.SILVER_CHARIOT);
    RegistrySupplier<StandType> MAGICIANS_RED = register("magicians_red", JEntityTypeRegistry.MAGICIANS_RED);
    RegistrySupplier<StandType> THE_FOOL = register("the_fool", JEntityTypeRegistry.THE_FOOL);
    RegistrySupplier<StandType> GOLD_EXPERIENCE = register("gold_experience", JEntityTypeRegistry.GOLD_EXPERIENCE);
    RegistrySupplier<StandType> HIEROPHANT_GREEN = register("hierophant_green", JEntityTypeRegistry.HIEROPHANT_GREEN);
    RegistrySupplier<StandType> THE_SUN = register("the_sun", JEntityTypeRegistry.THE_SUN);
    RegistrySupplier<StandType> PURPLE_HAZE = register("purple_haze", JEntityTypeRegistry.PURPLE_HAZE);
    RegistrySupplier<StandType> C_MOON = register("c_moon", JEntityTypeRegistry.C_MOON);
    RegistrySupplier<StandType> MADE_IN_HEAVEN = register("made_in_heaven", JEntityTypeRegistry.MADE_IN_HEAVEN);
    RegistrySupplier<StandType> THE_WORLD_OVER_HEAVEN = register("the_world_over_heaven", JEntityTypeRegistry.THE_WORLD_OVER_HEAVEN);
    RegistrySupplier<StandType> KILLER_QUEEN_BITES_THE_DUST = register("killer_queen_bites_the_dust", JEntityTypeRegistry.KILLER_QUEEN_BITES_THE_DUST);
    RegistrySupplier<StandType> GOLD_EXPERIENCE_REQUIEM = register("gold_experience_requiem", JEntityTypeRegistry.GER);
    RegistrySupplier<StandType> STAR_PLATINUM_THE_WORLD = register("star_platinum_the_world", JEntityTypeRegistry.SPTW);
    RegistrySupplier<StandType> PURPLE_HAZE_DISTORTION = register("purple_haze_distortion", JEntityTypeRegistry.PURPLE_HAZE_DISTORTION);
    RegistrySupplier<StandType> HORUS = register("horus", JEntityTypeRegistry.HORUS);
    RegistrySupplier<StandType> CINDERELLA = register("cinderella", JEntityTypeRegistry.CINDERELLA);
    RegistrySupplier<StandType> OSIRIS = register("osiris", JEntityTypeRegistry.OSIRIS);
    RegistrySupplier<StandType> ATUM = register("atum", JEntityTypeRegistry.ATUM);
    RegistrySupplier<StandType> CHARIOT_REQUIEM = register("chariot_requiem", JEntityTypeRegistry.CHARIOT_REQUIEM);
    RegistrySupplier<StandType> DIVER_DOWN = register("diver_down", JEntityTypeRegistry.DIVER_DOWN);
    RegistrySupplier<StandType> DRAGONS_DREAM = register("dragons_dream", JEntityTypeRegistry.DRAGONS_DREAM);
    RegistrySupplier<StandType> FOO_FIGHTERS = register("foo_fighters", JEntityTypeRegistry.FOO_FIGHTERS);
    RegistrySupplier<StandType> GOO_GOO_DOLLS = register("goo_goo_dolls", JEntityTypeRegistry.GOO_GOO_DOLLS);
    RegistrySupplier<StandType> SHADOW_THE_WORLD = register("shadow_the_world", JEntityTypeRegistry.SHADOW_THE_WORLD);
    RegistrySupplier<StandType> METALLICA = register("metallica", JEntityTypeRegistry.METALLICA);
    RegistrySupplier<StandType> THE_HAND = register("the_hand", JEntityTypeRegistry.THE_HAND);
    RegistrySupplier<StandType> MANDOM = register("mandom", JEntityTypeRegistry.MANDOM);
    RegistrySupplier<StandType> TCB = register("tcb", JEntityTypeRegistry.TCB);
    RegistrySupplier<StandType> CRAZY_DIAMOND = register("crazy_diamond", JEntityTypeRegistry.CRAZY_DIAMOND);
    RegistrySupplier<StandType> AEROSMITH = register("aerosmith", JEntityTypeRegistry.AEROSMITH);


    // Maps numeric values (ordinals) of old stand types back from when this was an enum.
    // Will be deleted in the 1.0 release.
    Int2ObjectMap<RegistrySupplier<StandType>> LEGACY_ORDINALS = Util.make(new Int2ObjectArrayMap<>(), map -> {
        List<RegistrySupplier<StandType>> entries = List.of(
                NONE,
                STAR_PLATINUM,
                THE_WORLD,
                KING_CRIMSON,
                D4C,
                CREAM,
                KILLER_QUEEN,
                WHITE_SNAKE,
                SILVER_CHARIOT,
                MAGICIANS_RED,
                THE_FOOL,
                GOLD_EXPERIENCE,
                HIEROPHANT_GREEN,
                THE_SUN,
                PURPLE_HAZE,
                C_MOON,
                MADE_IN_HEAVEN,
                THE_WORLD_OVER_HEAVEN,
                KILLER_QUEEN_BITES_THE_DUST,
                GOLD_EXPERIENCE_REQUIEM,
                STAR_PLATINUM_THE_WORLD,
                PURPLE_HAZE_DISTORTION,
                HORUS,
                CINDERELLA,
                OSIRIS,
                ATUM,
                CHARIOT_REQUIEM,
                DIVER_DOWN,
                DRAGONS_DREAM,
                FOO_FIGHTERS,
                GOO_GOO_DOLLS,
                SHADOW_THE_WORLD,
                METALLICA,
                THE_HAND
        );

        for (int i = 0; i < entries.size(); i++) {
            map.put(i, entries.get(i));
        }
    });

    /**
     * Internal use only.
     * Registers a new StandType with the given name and entity type supplier.
     * <p>
     * Add-on mods should make their own DeferredRegister and register their own StandTypes
     * with their own namespace using that instead of this method.
     * @param name The name (path part of the ResourceLocation) of the StandType.
     * @param entityTypeSupplier The supplier of the EntityType for the StandEntity.
     *                   Gotten by registering the stand entity type separately.
     * @return A Supplier that provides the registered StandType instance.
     */
    static <E extends StandEntity<?, ?>> RegistrySupplier<StandType> register(String name, Supplier<EntityType<E>> entityTypeSupplier) {
        ResourceLocation id = JCraft.id(name);
        return STAND_TYPE_REGISTRY.register(name, () -> StandType.of(id, entityTypeSupplier));
    }
}
