package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.world.item.ItemStack;

public class AuMockItem extends MockItem {

    public static ItemStack createMockStack(ItemStack stack) {
        // No need to create a mock stack if it already is one
        if (stack.getItem() instanceof AuMockItem) {
            return stack;
        }
        return MockItem.createMockStack(stack, JItemRegistry.AU_MOCK_ITEM);
    }

}
