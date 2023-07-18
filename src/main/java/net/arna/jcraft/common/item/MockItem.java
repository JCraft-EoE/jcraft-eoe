package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MockItem extends Item {
    private static final ItemStack FALLBACK = new ItemStack(Items.DIRT);

    public MockItem() {
        super(new Settings()
                .group(JCraft.JCRAFT_GROUP));
    }

    public static boolean isMockItem(ItemStack stack) {
        return stack.isOf(JObjectRegistry.MOCK_ITEM);
    }

    public static ItemStack getMockedStack(ItemStack mockItemStack) {
        NbtCompound nbt = mockItemStack.getNbt();
        if (nbt == null || !nbt.contains("MockItem", NbtElement.STRING_TYPE)) return FALLBACK;

        String mockItemId = nbt.getString("MockItem");
        Item mockItem = Registry.ITEM.get(new Identifier(mockItemId));

        NbtCompound mockData = nbt.contains("MockData", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("MockData") : null;

        ItemStack mockedStack = new ItemStack(mockItem, mockItemStack.getCount());
        mockedStack.setNbt(mockData);

        return mockedStack;
    }

    public static ItemStack createMockStack(ItemStack stack) {
        ItemStack mockStack = new ItemStack(JObjectRegistry.MOCK_ITEM, stack.getCount());
        NbtCompound nbt = mockStack.getOrCreateNbt();
        nbt.putString("MockItem", Registry.ITEM.getId(stack.getItem()).toString());
        if (stack.getNbt() != null) nbt.put("MockData", stack.getNbt());

        return mockStack;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return getMockedStack(stack).getTranslationKey();
    }
}
