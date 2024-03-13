package net.arna.jcraft.client.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;

import static net.arna.jcraft.common.item.BloodBottleItem.MAX_BLOOD;

public interface JModelPredicateProviderRegistry {
    static void register() {
        ModelPredicateProviderRegistry.register(
                JObjectRegistry.BLOOD_BOTTLE,
                JCraft.id("blood"),
                (stack, world, entity, seed) -> stack.getOrCreateNbt().getFloat("Blood") / MAX_BLOOD
        );
    }
}
