package net.arna.jcraft.client.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;

public class JModelPredicateProviderRegistry {
    public static void register() {
        ModelPredicateProviderRegistry.register(
                JObjectRegistry.BLOOD_BOTTLE,
                JCraft.id("blood"),
                (stack, world, entity, seed) -> stack.getOrCreateNbt().getFloat("Blood") / 16.0f
        );
    }
}
