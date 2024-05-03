package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatType;

public class JStatRegistry {
    //public static StatType<Item> TEST;

    private static <T> StatType<T> registerType(String id, Registry<T> registry) {
        return Registry.register(Registries.STAT_TYPE, JCraft.id(id), new StatType<>(registry));
    }

    public static void init() {
        //TEST = registerType("test", Registry.ITEM);
    }
}
