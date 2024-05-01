package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.minecraft.stat.StatType;
import net.minecraft.util.registry.Registry;

public class JStatRegistry {
    //public static StatType<Item> TEST;

    private static <T> StatType<T> registerType(String id, Registry<T> registry) {
        return Registry.register(Registry.STAT_TYPE, JCraft.id(id), new StatType<>(registry));
    }

    public static void init() {
        //TEST = registerType("test", Registry.ITEM);
    }
}
