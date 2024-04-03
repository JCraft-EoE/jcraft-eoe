package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.enchantments.CinderellasKissEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface JEnchantmentRegistry {
    Enchantment CINDERELLAS_KISS = register("cinderellas_kiss", CinderellasKissEnchantment.INSTANCE);
    
    static void init() {}
    
    static Enchantment register(String id, Enchantment enchantment) {
        Registry.register(Registries.ENCHANTMENT, JCraft.id(id), enchantment);
        return enchantment;
    }
}
